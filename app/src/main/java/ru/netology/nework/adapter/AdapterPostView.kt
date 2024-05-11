package ru.netology.nework.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nework.R
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.util.AndroidUtils
import javax.inject.Inject


interface OnIteractionListenerPostView {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun openLinkVideo(post: Post)
    fun openCardPost(id: Long)
}

class AdapterPostView @Inject constructor(
    private val binding: PostViewBinding,
    private val onListener: OnIteractionListenerPostView,
    private val listMentions: List<Post>,
    @ApplicationContext
    private val context: Context
) {

    private var mapView: MapView
    private var yandexMapsKitFactory: MapKit? = null

    init {
        yandexMapsKitFactory = MapKitFactory.getInstance()
        mapView = binding.map
        yandexMapsKitFactory?.onStart()
        mapView.onStart()
    }

    private val placemarkTapListener = MapObjectTapListener { _, point ->
//        Toast.makeText(
//            this@AppActivity,
//            "Tapped the point (${point.longitude}, ${point.latitude})",
//            Toast.LENGTH_SHORT
//        ).show()
        true
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bind(post: Post) {
        yandexMapsKitFactory = MapKitFactory.getInstance()
        mapView = binding.map

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

            layMaps.visibility = View.GONE
            post.coords?.let { layMaps.visibility = View.VISIBLE }

            post.attachment?.let {
                when (it.attachmentType) {
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

                    }

                    AttachmentType.AUDIO -> {

                    }

                    else -> return
                }

            }

            Glide.with(avatar)
                .load(post.authorAvatar)
//                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.icon_person_24)
                .timeout(45_000)
                .circleCrop()
                .into(avatar)

            listUsers.visibility = View.GONE
            if(post.mentionIds?.size!! > 0){
                layMentions.setOnClickListener{
                    println("post ${post.mentionIds}")
                    val users =
                        listOf("user1", "user2", "user1", "user2", "user1", "user2", "user1", "user2")
                    val adapter = ArrayAdapter<String>(context, android.R.layout.simple_gallery_item, users)
                    listUsers.adapter = adapter
                    if(listUsers.isVisible)listUsers.visibility = View.GONE
                    else listUsers.visibility = View.VISIBLE
                }

            }



            transparentImage.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
                val event = motionEvent.action
                when (event) {
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
            })
        }

    }
}