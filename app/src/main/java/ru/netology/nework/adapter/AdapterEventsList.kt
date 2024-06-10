package ru.netology.nework.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.MeetingType
import ru.netology.nework.util.AndroidUtils.getTimePublish

interface OnEventsListener{
    fun onLike(event: Event)
    fun onShare(event: Event)
    fun onEdit(event: Event)
    fun onRemove(event: Event)
    fun openCardEvent(event: Event)
    fun onParticipants(event: Event)
}

class AdapterEventsList(
    private val onEventsListener: OnEventsListener
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventsListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event!!)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventsListener: OnEventsListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        with(binding) {
            author.text = event.author
            published.text = getTimePublish(event.published)
            infoDate.text= event.typeMeeting.toString()
            if(event.typeMeeting == MeetingType.ONLINE) infoDate.setTextColor(Color.parseColor("#00ff00"))
            dateTime.text= getTimePublish(event.datetime)
            content.text = event.content

            icLike.isChecked = event.likedByMe?:false
            icLike.text = event.likeOwnerIds?.count().toString()
            icLike.setOnClickListener {
                onEventsListener.onLike(event)
            }

            icShare.setOnClickListener {
                onEventsListener.onShare(event)
            }
            icParticipants.isChecked = event.participatedByMe?:false
            icParticipants.text = event.participantsIds?.count().toString()
            icParticipants.setOnClickListener {
                onEventsListener.onParticipants(event)
            }

            root.setOnClickListener {
                onEventsListener.openCardEvent(event)
            }

            Glide.with(avatar)
                .load(event.authorAvatar)
                .error(R.drawable.icon_person_24)
                .timeout(180_000)
                .circleCrop()
                .into(avatar)

            imageView.visibility = View.GONE
            playAudio.visibility = View.GONE
            play.visibility = View.GONE
            event.attachment?.let {
                when (it.type) {
                    AttachmentType.IMAGE, null -> {
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView)
                            .load(event.attachment.url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            //.error(R.drawable.ic_error_100dp)
                            .timeout(180_000)
                            .into(imageView)
                    }

                    AttachmentType.VIDEO -> {
                        imageView.visibility = View.VISIBLE
                        play.visibility = View.VISIBLE
                        Glide.with(imageView)
                            .load(event.attachment.url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            //.error(R.drawable.ic_error_100dp)
                            .timeout(180_000)
                            .into(imageView)
                        play.setOnClickListener {
                            onEventsListener.openCardEvent(event)
                        }
                    }

                    AttachmentType.AUDIO -> {
                        playAudio.visibility = View.VISIBLE
                    }
                }

            }

            menu.isVisible = event.eventOwner
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onEventsListener.onEdit(event)
                                true
                            }

                            R.id.remove -> {
                                onEventsListener.onRemove(event)
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

class EventDiffCallBack : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
        oldItem.id == newItem.id


    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
        oldItem == newItem


}
