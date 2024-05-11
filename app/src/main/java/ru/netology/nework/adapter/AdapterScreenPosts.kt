package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils.getTimePublish

interface OnIteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun openLinkVideo(post: Post)
    fun openCardPost(post: Post)
}

class AdapterScreenPosts(
    private val onIteractionListener: OnIteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onIteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onIteractionListener: OnIteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = getTimePublish(post.published)
            content.text = post.content
            icLike.isChecked = post.likedByMe
            icLike.text = post.likeOwnerIds?.count().toString()
            icLike.setOnClickListener {
                onIteractionListener.onLike(post)
            }
            icShare.setOnClickListener{
                onIteractionListener.onShare(post)
            }
            imageView.visibility = View.GONE
            post.attachment?.let {
                when (it.attachmentType) {
                    AttachmentType.IMAGE, null -> {
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView)
                            .load(post.attachment.url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            //.error(R.drawable.ic_error_100dp)
                            .timeout(180_000)
                            .into(imageView)
                    }

                    AttachmentType.VIDEO -> {
                        println("post ${post.id} VIDEO")
                    }

                    AttachmentType.AUDIO -> {
                        println("post ${post.id} AUDIO")
                    }

                    else -> return
                }

            }

            Glide.with(avatar)
                .load(post.authorAvatar)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.icon_person_24)
                .timeout(180_000)
                .circleCrop()
                .into(avatar)

            root.setOnClickListener {
                onIteractionListener.openCardPost(post)
            }

            menu.isVisible = post.postOwner

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIteractionListener.onEdit(post)
                                true
                            }

                            R.id.remove -> {
                                onIteractionListener.onRemove(post)
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

}