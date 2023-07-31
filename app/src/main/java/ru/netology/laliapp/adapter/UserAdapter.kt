package ru.netology.laliapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.CardUserBinding
import ru.netology.laliapp.databinding.CardUserHorizontalBinding
import ru.netology.laliapp.dto.User

interface OnUserInteractionListener {
    fun openProfile(user: User)
}

class UserAdapter(private val onUserInteractionListener: OnUserInteractionListener) :
    ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding, onUserInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class UserHorizontalAdapter(private val onUserInteractionListener: OnUserInteractionListener) :
    ListAdapter<User, UserViewHorizontalHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHorizontalHolder {
        val binding = CardUserHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHorizontalHolder(binding, onUserInteractionListener)
    }


    override fun onBindViewHolder(holder: UserViewHorizontalHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class UserViewHorizontalHolder(
    private val binding: CardUserHorizontalBinding,
    private val onUserInteractionListener: OnUserInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        with(binding) {
            textViewName.text = user.name
            Glide.with(imageViewAvatar)
                .load("${user.avatar}")
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(imageViewAvatar)

            userView.setOnClickListener {
                onUserInteractionListener.openProfile(user)
            }
        }
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserInteractionListener: OnUserInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        with(binding) {
            textViewName.text = user.name
            Glide.with(imageViewAvatar)
                .load("${user.avatar}")
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(imageViewAvatar)

            userView.setOnClickListener {
                onUserInteractionListener.openProfile(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}