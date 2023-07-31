package ru.netology.laliapp.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.CardEventBinding
import ru.netology.laliapp.dto.Coordinates
import ru.netology.laliapp.dto.Event
import ru.netology.laliapp.dto.STRING_FOR_LINK
import ru.netology.laliapp.enumeration.AttachmentType
import ru.netology.laliapp.utils.formatToDate

interface OnEventInteractionListener {
    fun onOpenEvent(event: Event)
    fun onEditEvent(event: Event)
    fun onRemoveEvent(event: Event)
    fun onOpenSpeakers(event: Event)
    fun onOpenMap(event: Event)
    fun onOpenImageAttachment(event: Event)
    fun onLikeEvent(event: Event)
    fun onParticipateEvent(event: Event)
    fun onShareEvent(event: Event)
    fun onOpenLikers(event: Event)
    fun onOpenParticipants(event: Event)
    fun onOpenAuthorProfile(event: Event)
}

class EventAdapter(
    private val onEventInteractionListener: OnEventInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding, onEventInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {


    fun bind(event: Event) {
        binding.apply {
            textViewAuthor.text = event.author
            textViewPublished.text = formatToDate(event.published)
            textViewContent.text = event.content
            textViewDateTime.text = formatToDate(event.datetime)
            checkboxSpeakers.text = event.speakerIds.count().toString()
            buttonLike.isChecked = event.likedByMe
            buttonLike.text = event.likeOwnerIds.count().toString()
            buttonParticipate.isChecked = event.participatedByMe
            buttonParticipate.text = event.participantsIds.count().toString()
            textViewType.text = event.type.toString()
            imageViewAttachmentImage.visibility =
                if (event.attachment != null && event.attachment.type == AttachmentType.IMAGE) VISIBLE else GONE
            buttonLink.visibility =
                if ((event.link == null) || (event.link == STRING_FOR_LINK)) GONE else VISIBLE
                        buttonLink.text = event.link.toString()

            Glide.with(itemView).load("${event.authorAvatar}")
                .placeholder(R.drawable.ic_baseline_loading_24)
                .error(R.drawable.ic_baseline_person_24).timeout(10_000).circleCrop()
                .into(imageViewAvatar)

            event.attachment?.apply {
                Glide.with(imageViewAttachmentImage).load(this.url)
                    .placeholder(R.drawable.ic_baseline_loading_24)
                    .error(R.drawable.ic_baseline_error_outline_24).timeout(10_000)
                    .into(imageViewAttachmentImage)
            }

            buttonMenu.isVisible = event.ownedByMe
            buttonMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, event.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onEventInteractionListener.onRemoveEvent(event)
                                true
                            }

                            R.id.edit -> {
                                onEventInteractionListener.onEditEvent(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            checkboxSpeakers.setOnClickListener {
                onEventInteractionListener.onOpenSpeakers(event)
            }

            imageViewAttachmentImage.setOnClickListener {
                event.attachment?.let {
                    onEventInteractionListener.onOpenImageAttachment(event)
                }
            }

            imageViewAvatar.setOnClickListener {
                onEventInteractionListener.onOpenAuthorProfile(event)
            }

            buttonLocation.visibility =
                if (event.coords == null || event.coords == Coordinates(0.0, 0.0)) GONE else VISIBLE

            buttonLocation.setOnClickListener {
                onEventInteractionListener.onOpenMap(event)
            }

            imageViewAttachmentImage.setOnClickListener {
                onEventInteractionListener.onOpenImageAttachment(event)
            }

            buttonLike.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1F, 1.2F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1F, 1.2F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                }.start()
                onEventInteractionListener.onLikeEvent(event)
            }

            buttonParticipate.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1F, 1.2F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1F, 1.2F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                }.start()
                onEventInteractionListener.onParticipateEvent(event)
            }

            buttonShare.setOnClickListener {
                onEventInteractionListener.onShareEvent(event)
            }

            buttonLike.setOnLongClickListener {
                onEventInteractionListener.onOpenLikers(event)
                true
            }

            buttonParticipate.setOnLongClickListener {
                onEventInteractionListener.onOpenParticipants(event)
                true
            }
        }

    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}