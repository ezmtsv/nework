package ru.netology.nework.adapter

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.yandex.mapkit.geometry.Point
import ru.netology.nework.R
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils
import javax.inject.Inject


interface OnIteractionListenerPostView {
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun playAudio(link: String)
    fun playVideo(link: String)
    fun openSpacePhoto(post: Post)
    fun showUsers(users: List<Long>?)
}

class AdapterPostView @Inject constructor(
    private val binding: PostViewBinding,
    private val onListener: OnIteractionListenerPostView,
    private val yakit: YaKit,
) {


    init {
        yakit.initMapView(binding.map)
    }

    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    fun bind(post: Post) {
        //       yandexMapsKitFactory = MapKitFactory.getInstance()
//        mapView = binding.map
//        println("post Id ${post.id} postAttach ${post.attachment}")
//        println("post likeOwnerIds ${post.likeOwnerIds}, post mentionIds ${post.mentionIds}, users ${post.users}")
        binding.apply {
            author.text = post.author
            published.text = AndroidUtils.getTimePublish(post.published)
            content.text = post.content
            icLike.isChecked = post.likedByMe
            icLike.text = post.likeOwnerIds?.count().toString()
            icLike.setOnClickListener {
                onListener.onLike(post)
            }
            link.visibility = View.GONE
            post.link?.let {
                link.visibility = View.VISIBLE
                link.text = post.link
            }
            jobPlace.text = post.authorJob ?: "В поиске работы"
            imageView.visibility = View.GONE
            layAudio.visibility = View.GONE
            layMaps.visibility = View.GONE
            play.visibility = View.GONE
            post.coords?.let {
                if (it.lat != 0.0 && it.longCr != 0.0) {
                    layMaps.visibility = View.VISIBLE
                    val startLocation = Point(post.coords.lat!!, post.coords.longCr!!)
                    yakit.moveToStartLocation(startLocation)
                    yakit.setMarkerInStartLocation(startLocation)
                }
            }

            imageView.setOnClickListener {
                if (post.attachment?.type == AttachmentType.IMAGE) onListener.openSpacePhoto(post)
                if (post.attachment?.type == AttachmentType.VIDEO) {
                    videoView.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                    onListener.playVideo(post.attachment.url)
                    play.visibility = View.GONE
                }
            }

            post.attachment?.let {
                when (it.type) {
                    AttachmentType.IMAGE, null -> {
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView)
                            .load(post.attachment.url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            //.error(R.drawable.ic_error_100dp)
                            .timeout(45_000)
                            .into(imageView)
                    }

                    AttachmentType.VIDEO -> {
                        play.visibility = View.VISIBLE
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView)
                            .load(post.attachment.url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            //.error(R.drawable.ic_error_100dp)
                            .timeout(180_000)
                            .into(imageView)

                        play.setOnClickListener {
                            videoView.visibility = View.VISIBLE
                            imageView.visibility = View.GONE
                            onListener.playVideo(post.attachment.url)
                            play.visibility = View.GONE
                        }
                    }

                    AttachmentType.AUDIO -> {
                        layAudio.visibility = View.VISIBLE
                        playAudio.setOnClickListener { onListener.playAudio(post.attachment.url) }
                    }
                }

            }

            Glide.with(avatar)
                .load(post.authorAvatar)
//                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.icon_person_24)
                .timeout(45_000)
                .circleCrop()
                .into(avatar)

            icMentions.text = post.mentionIds?.count().toString()

            post.mentionIds?.let {
                val listUsersMentions = mutableListOf<UserPreview>()
                post.users?.forEach { (t, u) ->
                    if (post.mentionIds.contains(t.toLong()) && u.avatar != null) listUsersMentions.add(
                        u
                    )
                }

                val dimViewAvatarLikes = listOf(
                    avatarUser1,
                    avatarUser2,
                    avatarUser3,
                    avatarUser4,
                    avatarUser5
                )
                if (listUsersMentions.size < 6) {
                    for (i in 0..<listUsersMentions.size) {
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersMentions[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                } else {
                    for (i in 0..<5) {
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersMentions[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                    Glide.with(avatarUser6)
                        .load(R.drawable.but_plus)
                        .timeout(45_000)
                        .circleCrop()
                        .into(avatarUser6)

                }
            }
            post.likeOwnerIds?.let {
                val listUsersLike = mutableListOf<UserPreview>()
                post.users?.forEach { (t, u) ->
                    if (post.likeOwnerIds.contains(t.toLong()) && u.avatar != null) listUsersLike.add(
                        u
                    )
                }

                val dimViewAvatarLikes = listOf(
                    avatarUserLike1,
                    avatarUserLike2,
                    avatarUserLike3,
                    avatarUserLike4,
                    avatarUserLike5
                )
                for (i in 0..<5) {
                    dimViewAvatarLikes[i].visibility = View.GONE
                }
                if (listUsersLike.size < 6) {
                    for (i in 0..<listUsersLike.size) {
                        dimViewAvatarLikes[i].visibility = View.VISIBLE
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersLike[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                } else {
                    for (i in 0..<5) {
                        dimViewAvatarLikes[i].visibility = View.VISIBLE
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersLike[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                    avatarUserLike6.visibility = View.VISIBLE
                    Glide.with(avatarUserLike6)
                        .load(R.drawable.but_plus)
                        .timeout(45_000)
                        .circleCrop()
                        .into(avatarUserLike6)

                }
            }


            avatarUser6.setOnClickListener {
                onListener.showUsers(post.mentionIds)
            }
            avatarUserLike6.setOnClickListener {
                onListener.showUsers(post.likeOwnerIds)
            }

            transparentImage.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        scroll.requestDisallowInterceptTouchEvent(true)
                        false
                    }

                    MotionEvent.ACTION_UP -> {
                        scroll.requestDisallowInterceptTouchEvent(false)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        scroll.requestDisallowInterceptTouchEvent(true)
                        false
                    }

                    else -> {
                        true
                    }
                }

            }
        }

    }
}