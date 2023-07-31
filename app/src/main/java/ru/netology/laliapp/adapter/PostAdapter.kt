package ru.netology.laliapp.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View.*
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.CardPostBinding
import ru.netology.laliapp.dto.Post
import ru.netology.laliapp.enumeration.AttachmentType.*
import ru.netology.laliapp.utils.formatToDate

interface OnPostInteractionListener {
    fun onOpenPost(post: Post) {}
    fun onEditPost(post: Post) {}
    fun onRemovePost(post: Post) {}
    fun onLikePost(post: Post) {}
    fun onMentionPost(post: Post) {}
    fun onSharePost(post: Post) {}
    fun onOpenLikers(post: Post) {}
    fun onOpenMentions(post: Post) {}
    fun onPlayVideo(post: Post) {}
    fun onPlayAudio(post: Post) {}
    fun onOpenImageAttachment(post: Post) {}
    fun onOpenAuthorProfile(post:Post)
}

class PostsAdapter(
    private val onPostInteractionListener: OnPostInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onPostInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {

        binding.apply {
            textViewAuthor.text = post.author
            textViewPublished.text = formatToDate(post.published)
            textViewContent.text = post.content
            buttonLike.isChecked = post.likedByMe
            buttonLike.text = post.likeOwnerIds.count().toString()
            buttonMention.isChecked = post.mentionedMe
            buttonMention.text = post.mentionIds.count().toString()

            imageViewAttachmentImage.visibility =
                if (post.attachment != null && post.attachment.type == IMAGE) VISIBLE else GONE

            imageButtonAudio.visibility =
                if (post.attachment != null && post.attachment.type == AUDIO) VISIBLE else GONE

            imageButtonVideo.visibility =
                if (post.attachment != null && post.attachment.type == VIDEO) VISIBLE else GONE

            Glide.with(itemView)
                .load("${post.authorAvatar}")
                .placeholder(R.drawable.ic_baseline_loading_24)
                .error(R.drawable.ic_baseline_person_24)
                .fallback(R.drawable.ic_baseline_add_a_photo_24)
                .timeout(10_000)
                .circleCrop()
                .into(imageViewAvatar)

            post.attachment?.apply {
                Glide.with(imageViewAttachmentImage)
                    .load(this.url)
                    .placeholder(R.drawable.ic_baseline_loading_24)
                    .error(R.drawable.ic_baseline_error_outline_24)
                    .timeout(10_000)
                    .into(imageViewAttachmentImage)
            }

            buttonMenu.isVisible = post.ownedByMe
            buttonMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onPostInteractionListener.onRemovePost(post)
                                true
                            }

                            R.id.edit -> {
                                onPostInteractionListener.onEditPost(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            imageViewAvatar.setOnClickListener {
                onPostInteractionListener.onOpenAuthorProfile(post)
            }


            buttonLike.setOnClickListener {
                val scaleX = PropertyValuesHolder.ofFloat(SCALE_X, 1F, 1.2F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(SCALE_Y, 1F, 1.2F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
                }.start()
                onPostInteractionListener.onLikePost(post)
            }

            buttonMention.setOnClickListener {
                onPostInteractionListener.onMentionPost(post)
            }

            buttonShare.setOnClickListener {
                onPostInteractionListener.onSharePost(post)
            }

            buttonLike.setOnLongClickListener {
                onPostInteractionListener.onOpenLikers(post)
                true
            }

            buttonMention.setOnLongClickListener {
                onPostInteractionListener.onOpenMentions(post)
                true
            }


            imageButtonVideo.setOnClickListener {
                onPostInteractionListener.onPlayVideo(post)
            }

            imageButtonAudio.setOnClickListener {
                onPostInteractionListener.onPlayAudio(post)
            }

            imageViewAttachmentImage.setOnClickListener {
                onPostInteractionListener.onOpenImageAttachment(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Post, newItem: Post): Any = Unit
}