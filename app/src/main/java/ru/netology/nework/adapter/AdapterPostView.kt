package ru.netology.nework.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun showMentionUsers(listUsersId: List<Long>?)
}

class AdapterPostView @Inject constructor(
    private val binding: PostViewBinding,
    private val onListener: OnIteractionListenerPostView,
    @ApplicationContext
    private val context: Context
) {

    private var mapView: MapView
    private var yandexMapsKitFactory: MapKit? = null
    private lateinit var mapObjectCollection: MapObjectCollection
    private lateinit var placemarkMapObject: PlacemarkMapObject

    //    private var startLocation = Point(59.9402, 30.315)
    private var zoomValue: Float = 16.5f

    init {
        yandexMapsKitFactory = MapKitFactory.getInstance()
        mapView = binding.map
        yandexMapsKitFactory?.onStart()
        mapView.onStart()
//        moveToStartLocation(startLocation)
//        setMarkerInStartLocation(startLocation)
    }

    private fun moveToStartLocation(point: Point) {
        mapView.map.move(
            CameraPosition(point, zoomValue, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
    }

    private fun setMarkerInStartLocation(startLocation: Point) {
        val marker = R.drawable.ic_pin_black_png // Добавляем ссылку на картинку
        mapObjectCollection =
            mapView.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        placemarkMapObject = mapObjectCollection.addPlacemark(
            startLocation,
            ImageProvider.fromResource(context, marker)
        ) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.5f // Устанавливаем прозрачность метке
        placemarkMapObject.setText("Здесь!") // Устанавливаем текст сверху метки
    }

    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    fun bind(post: Post) {
        //       yandexMapsKitFactory = MapKitFactory.getInstance()
        mapView = binding.map
        println("post Id ${post.id} postAttach ${post.attachment}")
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
                layMaps.visibility = View.VISIBLE
                val startLocation = Point(post.coords.lat!!, post.coords.longCr!!)
                moveToStartLocation(startLocation)
                setMarkerInStartLocation(startLocation)
            }

            imageView.setOnClickListener {
                onListener.openSpacePhoto(post)
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
                if (listUsersLike.size < 6) {
                    for (i in 0..<listUsersLike.size) {
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersLike[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                } else {
                    for (i in 0..<5) {
                        Glide.with(dimViewAvatarLikes[i])
                            .load(listUsersLike[i].avatar)
                            .timeout(45_000)
                            .circleCrop()
                            .into(dimViewAvatarLikes[i])
                    }
                    Glide.with(avatarUserLike6)
                        .load(R.drawable.but_plus)
                        .timeout(45_000)
                        .circleCrop()
                        .into(avatarUserLike6)

                }
            }


            avatarUser6.setOnClickListener {
                onListener.showMentionUsers(post.mentionIds)
            }
            avatarUserLike6.setOnClickListener {
                onListener.showMentionUsers(post.likeOwnerIds)
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