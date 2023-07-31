package ru.netology.laliapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.FragmentNewEventBinding
import ru.netology.laliapp.dto.Coordinates
import ru.netology.laliapp.dto.Event
import ru.netology.laliapp.dto.STRING_FOR_LINK
import ru.netology.laliapp.enumeration.AttachmentType
import ru.netology.laliapp.enumeration.EventType
import ru.netology.laliapp.utils.*
import ru.netology.laliapp.viewmodel.EventViewModel
import java.util.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    var type: AttachmentType? = null

    private val eventViewModel by activityViewModels<EventViewModel>()

    private var fragmentNewEventBinding: FragmentNewEventBinding? = null

    private var latitude: Double? = null
    private var longitude: Double? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )

        (activity as AppCompatActivity).supportActionBar?.title =
            context?.getString(R.string.title_event)

        fragmentNewEventBinding = binding
        latitude = arguments?.getDouble("lat")
        longitude = arguments?.getDouble("long")

        var typeEvent = eventViewModel.edited.value!!.type
        val link= eventViewModel.edited.value?.link

        arguments?.textArg
            ?.let(binding.editTextEvent::setText)

        binding.editTextEvent.requestFocus()
        binding.editTextLink.setText(if (link == STRING_FOR_LINK) "" else link) /* костыль из-за бэкенда. Там link обязательное поле */

        val datetime = arguments?.getString("datetime")?.let {
            formatToDate(it)
        } ?: formatToDate("${eventViewModel.edited.value?.datetime}")
        val date = datetime.substring(0, 10)
        val time = datetime.substring(11)

        binding.editTextEvent.setText(
            arguments?.getString("content") ?: eventViewModel.edited.value?.content
        )
        if (eventViewModel.edited.value != Event.emptyEvent) {
            binding.editTextDate.setText(date)
            binding.editTextTime.setText(time)
        }

        binding.imageViewPickGeo.setOnClickListener {
            eventViewModel.changeContent(
                content = binding.editTextEvent.text.toString(),
                date = formatToInstant(
                    "${binding.editTextDate.text}" +
                            " " +
                            "${binding.editTextTime.text}"
                ),
                coords = Coordinates(lat = latitude, long = longitude),
                link = if ((binding.editTextLink.text == null) || (binding.editTextLink.text.toString() == ""))
                    STRING_FOR_LINK
                else binding.editTextLink.text.toString(),
                type = typeEvent,
            )
            val bundle = Bundle().apply {
                putString("open", "newCoords")
                if (latitude != null) {
                    putDouble("lat", latitude!!)
                }
                if (longitude != null) {
                    putDouble("long", longitude!!)
                }
            }
            findNavController().navigate(R.id.nav_map_fragment, bundle)
        }

        binding.editTextDate.setOnClickListener {
            context?.let { item ->
                pickDate(binding.editTextDate, item)
            }
        }

        binding.editTextTime.setOnClickListener {
            context?.let { item ->
                pickTime(binding.editTextTime, item)
            }
        }



        binding.buttonAddSpeakers.setOnClickListener {

            val bundle = Bundle().apply {
                putString("open", "speaker")
            }
            findNavController().navigate(R.id.nav_users, bundle)
        }

        eventViewModel.edited.observe(viewLifecycleOwner) {
            binding.buttonAddSpeakers.apply {
                text = "$text ${eventViewModel.edited.value?.speakerIds?.count().toString()}"
            }
        }

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it.data?.data.let { uri ->
                    val stream = uri?.let {
                        context?.contentResolver?.openInputStream(it)
                    }
                    eventViewModel.changeMedia(uri, stream, type)
                }
            }

        binding.imageViewPickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .galleryOnly()
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                        "image/jpg"
                    )
                )
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
            type = AttachmentType.IMAGE
        }

        binding.imageViewTakePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
        }

        binding.buttonRemovePhoto.setOnClickListener {
            eventViewModel.changeMedia(null, null, null)
        }

        binding.radioButtonOnlineEvent.isChecked = typeEvent == EventType.ONLINE
        binding.radioButtonOfflineEvent.isChecked = typeEvent == EventType.OFFLINE

        binding.radioGroupTypeEvent.setOnCheckedChangeListener { _, id ->
            when (id) {

                binding.radioButtonOnlineEvent.id -> {
                    binding.radioButtonOnlineEvent.isChecked = true
                    binding.imageViewPickGeo.visibility = View.GONE
                    typeEvent = EventType.ONLINE
                }

                binding.radioButtonOfflineEvent.id -> {
                    binding.radioButtonOfflineEvent.isChecked = true
                    binding.imageViewPickGeo.visibility = View.VISIBLE
                    typeEvent = EventType.OFFLINE
                }
            }
        }

        if(typeEvent == EventType.ONLINE)  binding.imageViewPickGeo.visibility = View.GONE else View.VISIBLE

                eventViewModel.media.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                binding.frameLayoutPhoto.visibility = View.GONE
                return@observe
            }
            binding.frameLayoutPhoto.visibility = View.VISIBLE
            binding.imageViewPhoto.setImageURI(it.uri)
        }

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.nav_events)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.create_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentNewEventBinding?.let {
                            if (it.editTextEvent.text.isNullOrBlank()) {
                                Toast.makeText(
                                    activity,
                                    R.string.error_empty_content,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                /* на бэкенде считают, что coords должны быть по-любому */
                                if (latitude == null) latitude = 0.0
                                if (longitude == null) longitude = 0.0
                                eventViewModel.changeContent(
                                    content = it.editTextEvent.text.toString(),
                                    date = formatToInstant(
                                        "${it.editTextDate.text} " +
                                                "${it.editTextTime.text}"
                                    ),
                                    coords = Coordinates(latitude, longitude),
                                    link = if ((it.editTextLink.text == null) || (it.editTextLink.text.toString() == "")) STRING_FOR_LINK else it.editTextLink.text.toString(),
                                    type = typeEvent,
                                )
                                eventViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        return binding.root
    }

    override fun onDestroyView() {
        fragmentNewEventBinding = null
        super.onDestroyView()
    }
}