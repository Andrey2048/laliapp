package ru.netology.laliapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.laliapp.R
import ru.netology.laliapp.adapter.EventAdapter
import ru.netology.laliapp.adapter.OnEventInteractionListener
import ru.netology.laliapp.databinding.FragmentEventsBinding
import ru.netology.laliapp.dto.Event
import ru.netology.laliapp.viewmodel.AuthViewModel
import ru.netology.laliapp.viewmodel.EventViewModel
import ru.netology.laliapp.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val eventViewModel by activityViewModels<EventViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentEventsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = EventAdapter(object : OnEventInteractionListener {

            override fun onOpenEvent(event: Event) {}

            override fun onEditEvent(event: Event) {
                eventViewModel.edit(event)
                val bundle = Bundle().apply {
                    putString("content", event.content)
                    putString("dateTime", event.datetime)
                    putString("link", event.link)
                    event.coords?.lat?.let {
                        putDouble("lat", it)
                    }
                    event.coords?.long?.let {
                        putDouble("long", it)
                    }

                }
                findNavController()
                    .navigate(R.id.nav_new_event_fragment, bundle)
            }

            override fun onRemoveEvent(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun onOpenSpeakers(event: Event) {
                userViewModel.getUsersIds(event.speakerIds)
                if (event.speakerIds.isEmpty()) {
                    Toast.makeText(context, R.string.no_speakers, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_nav_bottom_sheet_fragment)
                }
            }

            override fun onOpenMap(event: Event) {
                val bundle = Bundle().apply {
                    event.coords?.lat?.let {
                        putDouble("lat", it)
                    }
                    event.coords?.long?.let {
                        putDouble("long", it)
                    }
                    putString("open", "coords")
                }

                findNavController().navigate(R.id.nav_map_fragment, bundle)
            }

            override fun onOpenImageAttachment(event: Event) {
                val bundle = Bundle().apply {
                    putString("url", event.attachment?.url)
                }
                findNavController().navigate(R.id.nav_image_attachment_fragment, bundle)
            }

            override fun onLikeEvent(event: Event) {
                if (authViewModel.authorized) {
                    if (!event.likedByMe)
                        eventViewModel.likeById(event.id)
                    else eventViewModel.unlikeById(event.id)
                } else {
                    Toast.makeText(activity, R.string.error_auth, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onParticipateEvent(event: Event) {
                if (authViewModel.authorized) {
                    if (!event.participatedByMe)
                        eventViewModel.participate(event.id)
                    else eventViewModel.doNotParticipate(event.id)
                } else {
                    Toast.makeText(activity, R.string.error_auth, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onShareEvent(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, "Share Event")
                startActivity(shareIntent)
            }

            override fun onOpenLikers(event: Event) {
                userViewModel.getUsersIds(event.likeOwnerIds)
                if (event.likeOwnerIds.isEmpty()) {
                    Toast.makeText(context, R.string.no_likers, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_nav_bottom_sheet_fragment)
                }
            }

            override fun onOpenParticipants(event: Event) {
                userViewModel.getUsersIds(event.participantsIds)
                if (event.participantsIds.isEmpty()) {
                    Toast.makeText(context, R.string.no_participants, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    findNavController().navigate(R.id.action_nav_events_to_nav_bottom_sheet_fragment)
                }
            }

            override fun onOpenAuthorProfile(event: Event) {

                userViewModel.getUserById(event.authorId)
                val bundle = Bundle().apply {
                    putLong("id", event.authorId)
                    putString("name", event.author)
                    putString("avatar", event.authorAvatar)
                }
                findNavController().apply {
                    this.navigate(R.id.nav_profile, bundle)
                }
            }
        })

        binding.recyclerViewContainer.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventViewModel.data.collectLatest(adapter::submitData)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                    binding.textViewEmptyText.isVisible =
                        adapter.itemCount < 1
                }
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            if (!authViewModel.authorized) {
                binding.fabAddEvent.visibility = View.GONE
            }
        }

        binding.fabAddEvent.setOnClickListener {
            eventViewModel.edit(Event.emptyEvent)
            findNavController().navigate(R.id.action_nav_events_to_nav_new_event_fragment)
        }

        eventViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        return binding.root
    }
}