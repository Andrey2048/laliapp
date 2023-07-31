package ru.netology.laliapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.CardJobBinding
import ru.netology.laliapp.dto.DATE_WORK_NOW
import ru.netology.laliapp.dto.Job
import ru.netology.laliapp.dto.STRING_FOR_LINK
import ru.netology.laliapp.utils.AndroidUtils

interface OnJobInteractionListener {
    fun onRemoveJob(job: Job)
    fun onEditJob(job: Job)
}

class JobAdapter(
    private val onJobInteractionListener: OnJobInteractionListener,
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(parent.context, binding, onJobInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

class JobViewHolder(
    private val context: Context,
    private val binding: CardJobBinding,
    private val onJobInteractionListener: OnJobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {

        binding.apply {
            textViewName.text = job.name
            textViewPosition.text = job.position
            textViewStart.text = AndroidUtils.convertDate(job.start).substring(0, 10)
            textViewFinish.text =
                if (job.finish == DATE_WORK_NOW) context.getString(R.string.text_job_now)
                else AndroidUtils.convertDate(job.finish).substring(0, 10)

            textViewLink.setText(if (job.link == STRING_FOR_LINK) "" else job.link) /* костыль из-за бэкенда. Там link и finish обязательные поля */

                if (job.link != STRING_FOR_LINK) {
                    textViewLinkText.visibility = VISIBLE
                    textViewLink.visibility = VISIBLE
                }  else {
                    textViewLinkText.visibility = GONE
                    textViewLink.visibility = GONE
                }

            buttonMenu.isVisible = job.ownedByMe
            buttonMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, job.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onJobInteractionListener.onRemoveJob(job)
                                true
                            }
                            R.id.edit -> {
                                onJobInteractionListener.onEditJob(job)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}