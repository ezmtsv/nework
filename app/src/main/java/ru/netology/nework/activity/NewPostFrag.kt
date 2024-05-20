package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.databinding.NewPostBinding
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.UnknownError
import ru.netology.nework.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import java.io.InputStream

const val MAX_SIZE_FILE = 15_728_640L

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint


class NewPostFrag : Fragment() {

    private val listUsers = mutableListOf<Long>()
    private var mapView: MapView? = null
    private var yandexMapsKitFactory: MapKit? = null
    private lateinit var mapObjectCollection: MapObjectCollection
    private lateinit var placemarkMapObject: PlacemarkMapObject
    private var zoomValue: Float = 12.5f

    val viewModelPosts: PostsViewModel by viewModels()
    private val viewModelUsers: UsersViewModel by viewModels()

    private val inputListener =  object: InputListener {
        override fun onMapTap(p0: Map, p1: Point) { }

        override fun onMapLongTap(p0: Map, p1: Point) {
            viewModelPosts.setLocation(Point(p1.latitude, p1.longitude))
        }
    }

    companion object {
        private var multiPartBody: MultipartBody.Part? = null
    }

    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = NewPostBinding.inflate(layoutInflater)

//        val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
//        vibrator.vibrate(20)

        var typeAttach: AttachmentType? = null
        var lastStateLoading = false

        yandexMapsKitFactory = MapKitFactory.getInstance()
        mapView = binding.map
        yandexMapsKitFactory?.onStart()
        mapView!!.onStart()


        val startLocation = Point(55.75, 37.62)
        moveToStartLocation(startLocation)
        setMarkerInStartLocation(startLocation)

        mapView!!.map.addInputListener(inputListener)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (binding.listUsers.isVisible) {
                            binding.listUsers.visibility = View.GONE
                            multiPartBody?.let {
                                binding.groupLoading.visibility = View.VISIBLE
                            }

                            binding.layMaps.visibility = View.VISIBLE
                        } else {
                            if (!binding.content.text.isNullOrBlank()) {
                                val txt = binding.content.text.toString()
                                var coords: Coordinates? = null
                                viewModelPosts.location.value?.let {
                                   coords = Coordinates().copy(it.latitude, it.longitude)
                                }
                                val post = Post(
                                    id = 0,
                                    authorId = 0,
                                    content = txt,
                                    mentionIds = listUsers,
                                    coords = coords
                                )
                                viewModelPosts.savePost(post, multiPartBody, typeAttach)
                            } else {
                                context?.toast("Для создания поста нужен контент!")
                            }
                        }
                        true
                    }

                    android.R.id.home -> {
                        if (binding.listUsers.isVisible) {
                            binding.listUsers.visibility = View.GONE
                            binding.layMaps.visibility = View.VISIBLE
                            multiPartBody?.let {
                                binding.groupLoading.visibility = View.VISIBLE
                            }
                        } else findNavController().navigateUp()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)

        fun getFileName(uri: Uri): String? {
            var result: String? = null
            if (uri.scheme.equals("content")) {
                val cursor = context?.contentResolver?.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        result = cursor.getString(index)
                    }
                } catch (e: Exception) {
                    println(e.printStackTrace())
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != -1) {
                    result = result?.substring(cut!! + 1)
                }
            }
            return result
        }

        fun uploadStream(inputStream: InputStream, type: String, uri: Uri): MultipartBody.Part {
//            println( "NAME ${getFileName(uri)}")
            val name = getFileName(uri)
            return MultipartBody.Part.createFormData(
                "file", name, RequestBody.create(
                    type.toMediaTypeOrNull(),
                    inputStream.readBytes()
                )
            )
        }

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
//                    println("resultCode ${it.resultCode}")
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    when (typeAttach) {
                        AttachmentType.IMAGE -> {
                            val file = uri?.toFile()
                            if (file?.length()!! < MAX_SIZE_FILE) {
                                viewModelPosts.changePhoto(uri, file)
                                multiPartBody = MultipartBody.Part.createFormData(
                                    "file", file.name, file.asRequestBody()
                                )
                                binding.btnClear.visibility = View.VISIBLE
                            } else {
                                context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
                            }
                        }

                        AttachmentType.AUDIO -> {
                            it.data?.data?.let { uri ->
                                val inputStream = context?.contentResolver?.openInputStream(uri)
                                val type = context?.contentResolver?.getType(uri)
                                if (type != null) {
                                    if (type.contains(Regex("audio/"))) {
                                        multiPartBody = inputStream?.let { stream ->
                                            uploadStream(stream, type, uri)
                                        }
                                        binding.groupLoading.visibility = View.VISIBLE
                                        binding.icAudio.setImageResource(R.drawable.play_circle_70)
                                        binding.nameTrack.text = getFileName(uri)
                                    } else {
                                        multiPartBody = null
                                        context?.toast("Неправильный формат файла, загрузите аудио файл!")
                                    }
                                }
                            }

                        }

                        AttachmentType.VIDEO -> {
                            it.data?.data?.let { uri ->
                                val inputStream = context?.contentResolver?.openInputStream(uri)
                                val type = context?.contentResolver?.getType(uri)

//                                multiPartBody = inputStream?.let { stream ->
//                                    type?.let { type ->
//                                        uploadStream(stream, type, uri)
//                                    }
//                                }
                                if (type != null) {
                                    if (type.contains(Regex("video/"))) {
                                        multiPartBody = inputStream?.let { stream ->
                                            uploadStream(stream, type, uri)
                                        }
                                        binding.groupLoading.visibility = View.VISIBLE
                                        binding.icAudio.setImageResource(R.drawable.video_file_70)
                                        binding.nameTrack.text = getFileName(uri)
                                    } else {
                                        multiPartBody = null
                                        context?.toast("Неправильный формат файла, загрузите видео файл!")
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }

                Activity.RESULT_CANCELED -> {
                    multiPartBody = null
                }
            }
        }


        binding.bottomNavigationNewPost.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_pic -> {
                    binding.selectAttach.visibility = View.GONE
                    multiPartBody = null
                    typeAttach = AttachmentType.IMAGE
                    ImagePicker.with(this)
                        .galleryOnly()
                        .crop()
                        .maxResultSize(2048, 2048)
                        .createIntent(launcher::launch)
                    true
                }

                R.id.add_geo -> {
                    binding.layMaps.visibility = View.VISIBLE
                    true
                }

                R.id.add_users -> {
                    binding.listUsers.visibility = View.VISIBLE
                    multiPartBody?.let {
                        binding.groupLoading.visibility = View.GONE
                    }
                    binding.layMaps.visibility = View.GONE
                    binding.selectAttach.visibility = View.GONE
                    true
                }

                R.id.photo -> {
                    binding.selectAttach.visibility = View.GONE
                    multiPartBody = null
                    binding.groupLoading.visibility = View.GONE
                    typeAttach = AttachmentType.IMAGE
                    ImagePicker.with(this)
                        .cameraOnly()
                        .crop()
                        .maxResultSize(2048, 2048)
                        .createIntent(launcher::launch)
                    true
                }

                R.id.add_file -> {
                    binding.groupImg.visibility = View.GONE
                    multiPartBody = null
                    binding.selectAttach.visibility = View.VISIBLE
                    true
                }

                else -> false
            }
        }


        val adapterUsers = AdapterUsersList(object : ListenerSelectionUser {
            override fun selectUser(user: UserResponse?) {

            }

            override fun addUser(idUser: Long?) {
                listUsers.add(idUser!!)
            }

            override fun removeUser(idUser: Long?) {
                if (listUsers.contains(idUser)) {
                    listUsers.remove(idUser)
                }
            }
        }, true)

        binding.listUsers.adapter = adapterUsers
        viewModelUsers.listUsers.observe(viewLifecycleOwner) { users ->
            adapterUsers.submitList(users)
        }
        with(binding) {

            attachAudio.setOnClickListener {
                typeAttach = AttachmentType.AUDIO
                launcher.launch(getIntent())
                selectAttach.visibility = View.GONE
            }
            attachVideo.setOnClickListener {
                typeAttach = AttachmentType.VIDEO
                launcher.launch(getIntent())
                selectAttach.visibility = View.GONE
            }
            btnClearLoading.setOnClickListener {
                groupLoading.visibility = View.GONE
                multiPartBody = null
            }
        }

        viewModelPosts.photo.observe(viewLifecycleOwner) {
            if (it == viewModelPosts.noPhoto) {
                binding.photo.visibility = View.GONE
                binding.content.focusAndShowKeyboard()
                typeAttach = null
                return@observe
            }
            typeAttach = AttachmentType.IMAGE
            binding.content.clearFocus()
            binding.photo.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        viewModelPosts.dataState.observe(viewLifecycleOwner) {
            if (it.loading) binding.btnClear.visibility = View.GONE
            if (!it.loading && lastStateLoading) findNavController().navigateUp()
            binding.progress.isVisible = it.loading
            lastStateLoading = it.loading
        }

        viewModelPosts.location.observe(viewLifecycleOwner){
            mapObjectCollection.clear()
            setMarkerInStartLocation(it)
            //vibrator.vibrate(70)
            vibratePhone()
        }

        binding.btnClear.setOnClickListener {
            viewModelPosts.clearPhoto()
            it.visibility = View.GONE
            multiPartBody = null
        }

        return binding.root
    }


    private var curFrag: CurrentShowFragment? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            curFrag = context as CurrentShowFragment
        } catch (e: ClassCastException) {
            throw UnknownError
        }
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach()
        curFrag = null
    }

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach(getString(R.string.new_post))

    }

    override fun onStop() {
        //binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun getIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*" // That's needed for some reason, crashes otherwise
        putExtra(
            // List all file types you want the user to be able to select
            Intent.EXTRA_MIME_TYPES, arrayOf(
//                "text/html", // .html
//                "text/plain", // .txt
//                "application/pdf",
                "audio/mpeg",
                "video/mp4",
            )
        )

    }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun moveToStartLocation(point: Point) {
        mapView?.map?.move(
            CameraPosition(point, zoomValue, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
    }

    private fun setMarkerInStartLocation(startLocation: Point) {
        val marker = R.drawable.ic_pin_black_png
        mapObjectCollection =
            mapView?.map!!.mapObjects
        placemarkMapObject = mapObjectCollection.addPlacemark(
            startLocation,
            ImageProvider.fromResource(context, marker)
        ) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.5f
        placemarkMapObject.setText("Здесь!")
    }

    private fun Fragment.vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(70)
        }
    }

}