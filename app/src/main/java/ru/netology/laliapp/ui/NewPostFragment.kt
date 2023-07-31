package ru.netology.laliapp.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.FragmentNewPostBinding
import ru.netology.laliapp.enumeration.AttachmentType
import ru.netology.laliapp.utils.AndroidUtils
import ru.netology.laliapp.viewmodel.PostViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment : Fragment() {

val bundle = Bundle().apply {  }
    var type: AttachmentType? = null

    private val postViewModel by activityViewModels<PostViewModel>()

    private var fragmentNewPostBinding: FragmentNewPostBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        (activity as AppCompatActivity).supportActionBar?.title =
            context?.getString(R.string.title_post)

        fragmentNewPostBinding = binding

        arguments?.getString("content")
        ?.let(binding.editTextContent::setText) ?: postViewModel._edited.value?.content
        binding.editTextContent.requestFocus()

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it.data?.data.let { uri ->
                    val stream = uri?.let {
                        context?.contentResolver?.openInputStream(it)
                    }
                    postViewModel.changeMedia(uri, stream, type)
                }
            }

        val mediaLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    val stream = context?.contentResolver?.openInputStream(it)
                    postViewModel.changeMedia(it, stream, type)
                }
            }

        binding.imageViewTakePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoLauncher::launch)
            type = AttachmentType.IMAGE
        }

        binding.imageViewPickPhoto.setOnClickListener {
            mediaLauncher.launch("image/*")
            type = AttachmentType.IMAGE
        }

        binding.imageViewPickAudio.setOnClickListener {
            mediaLauncher.launch("audio/*")
            type = AttachmentType.AUDIO
        }

        binding.imageViewPickVideo.setOnClickListener {
            mediaLauncher.launch("video/*")
            type = AttachmentType.VIDEO
        }

        binding.buttonRemovePhoto.setOnClickListener {
            postViewModel.changeMedia(null, null, null)
        }

        postViewModel.media.observe(viewLifecycleOwner) {
            if (it?.uri == null) {
                binding.frameLayoutMedia.visibility = View.GONE
                return@observe
            }
            binding.frameLayoutMedia.visibility = View.VISIBLE
            binding.imageViewPhoto.setImageURI(it.uri)
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.nav_feed)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.create_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentNewPostBinding?.let {
                            if (it.editTextContent.text.isNullOrBlank()) {
                                Toast.makeText(
                                    activity,
                                    R.string.error_empty_content,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                postViewModel.changeContent(
                                    binding.editTextContent.text.toString()
                                )
                                postViewModel.save()
                                AndroidUtils.hideKeyboard(requireView())
                            }
                        }
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)

        postViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.progressBar.isVisible = it.loading
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentNewPostBinding = null
        super.onDestroyView()
    }
}