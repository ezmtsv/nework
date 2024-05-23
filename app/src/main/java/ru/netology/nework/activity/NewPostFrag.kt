package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.InputListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.postArg
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.NewPostBinding
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.NewPostStatusModel
import ru.netology.nework.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import java.io.InputStream
import javax.inject.Inject

const val MAX_SIZE_FILE = 15_728_640L
const val SHOW = View.VISIBLE
const val HIDE = View.GONE

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint


class NewPostFrag : Fragment() {

    private val listUsers = mutableListOf<Long>()
    private var post: Post? = null

    @Inject
    lateinit var yakit: YaKit
    val viewModelPosts: PostsViewModel by viewModels()
    private val viewModelUsers: UsersViewModel by viewModels()

    private val inputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {}

        override fun onMapLongTap(p0: Map, p1: Point) {
            viewModelPosts.setLocation(Point(p1.latitude, p1.longitude))
        }
    }

    companion object {
        private var multiPartBody: MultipartBody.Part? = null
//        var typeAttach: AttachmentType? = null
    }

    @SuppressLint("Recycle")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = NewPostBinding.inflate(layoutInflater)
        yakit.initMapView(binding.map)

        var lastStateLoading = false
        post = arguments?.postArg

        post?.let {
//            binding.content.setText(post?.content.toString())
//            it.coords?.let {coordinates ->
//                viewModelPosts.setLocation(Point(coordinates.lat!!, coordinates.longCr!!))
//            }

        }

        val startLocation = Point(55.75, 37.62)
        yakit.moveToStartLocation(startLocation)
        yakit.setMarkerInStartLocation(startLocation)

        binding.map.map.addInputListener(inputListener)

        fun closeListUser() {
            when (viewModelPosts.typeAttach.value) {
                AttachmentType.IMAGE -> {
                    val statusLoading =
                        viewModelPosts.newPostStatusModel.value?.statusLoading
                            ?: false
                    val statusCoords =
                        viewModelPosts.newPostStatusModel.value?.statusCoords
                            ?: false
                    if (multiPartBody != null) {
                        viewModelPosts.setStatusNewPostFrag(
                            NewPostStatusModel(
                                groupImage = SHOW,
                                statusCoords = statusCoords,
                                statusLoading = statusLoading
                            )
                        )
                    } else {
                        NewPostStatusModel(
                            statusCoords = statusCoords,
                            statusLoading = statusLoading
                        )
                    }
                }

                AttachmentType.AUDIO, AttachmentType.VIDEO -> {
                    val statusLoading =
                        viewModelPosts.newPostStatusModel.value?.statusLoading
                            ?: false
                    val statusCoords =
                        viewModelPosts.newPostStatusModel.value?.statusCoords
                            ?: false
                    viewModelPosts.setStatusNewPostFrag(
                        NewPostStatusModel(
                            groupLoadFile = SHOW,
                            statusCoords = statusCoords,
                            statusLoading = statusLoading
                        )
                    )
                }

                else -> viewModelPosts.setStatusNewPostFrag(NewPostStatusModel())
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        if (binding.listUsers.isVisible) {
                            closeListUser()
                        } else {
                            if (binding.content.text.isNullOrBlank() && multiPartBody == null) {
                                context?.toast("Для создания поста нужен контент!")
                            } else {
                                val txt = binding.content.text.toString()
                                var coords: Coordinates? = null
                                viewModelPosts.location.value?.let {
                                    coords = Coordinates().copy(it.latitude, it.longitude)
                                }
//                                println("myID $myID")
                                val post = Post(
                                    id = 0,
                                    authorId = myID ?: 0,
                                    content = txt,
                                    mentionIds = listUsers,
                                    coords = coords
                                )
                                yakit.stopMapView()
                                viewModelPosts.savePost(
                                    post,
                                    multiPartBody,
                                    viewModelPosts.typeAttach.value
                                )
                            }
                        }
                        true
                    }

                    android.R.id.home -> {
                        if (binding.listUsers.isVisible) {
                            closeListUser()
                        } else findNavController().navigateUp()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)

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
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(it.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    when (viewModelPosts.typeAttach.value) {
                        AttachmentType.IMAGE -> {
                            val file = uri?.toFile()
                            if (file?.length()!! < MAX_SIZE_FILE) {
                                viewModelPosts.changePhoto(uri, file)
                                multiPartBody = MultipartBody.Part.createFormData(
                                    "file", file.name, file.asRequestBody()
                                )
                                val statusCoords =
                                    viewModelPosts.newPostStatusModel.value?.statusCoords ?: false
                                viewModelPosts.setStatusNewPostFrag(
                                    NewPostStatusModel(
                                        groupImage = SHOW,
                                        statusCoords = statusCoords
                                    )
                                )
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

                                        val statusCoords =
                                            viewModelPosts.newPostStatusModel.value?.statusCoords
                                                ?: false
                                        viewModelPosts.setStatusNewPostFrag(
                                            NewPostStatusModel(
                                                groupLoadFile = SHOW,
                                                statusLoading = true,
                                                statusCoords = statusCoords
                                            )
                                        )

                                        Glide.with(binding.icAudio)
                                            .load(R.drawable.play_circle_70)
                                            .into(binding.icAudio)
                                        binding.nameTrack.text = getFileName(uri)
                                    } else {
                                        multiPartBody = null
                                        viewModelPosts.setTypeAttach(null)
                                        context?.toast("Неправильный формат файла, загрузите аудио файл!")
                                    }
                                }
                            }

                        }

                        AttachmentType.VIDEO -> {
                            it.data?.data?.let { uri ->
                                val inputStream = context?.contentResolver?.openInputStream(uri)
                                val type = context?.contentResolver?.getType(uri)
                                if (type != null) {
                                    if (type.contains(Regex("video/"))) {
                                        multiPartBody = inputStream?.let { stream ->
                                            uploadStream(stream, type, uri)
                                        }


                                        val statusCoords =
                                            viewModelPosts.newPostStatusModel.value?.statusCoords
                                                ?: false
                                        viewModelPosts.setStatusNewPostFrag(
                                            NewPostStatusModel(
                                                groupLoadFile = SHOW,
                                                statusLoading = true,
                                                statusCoords = statusCoords
                                            )
                                        )
                                        Glide.with(binding.icAudio)
                                            .load(R.drawable.video_file_70)
                                            .into(binding.icAudio)
                                        binding.nameTrack.text = getFileName(uri)
                                    } else {
                                        multiPartBody = null
                                        viewModelPosts.setTypeAttach(null)
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
                    viewModelPosts.setTypeAttach(null)
                }
            }
        }


        binding.bottomNavigationNewPost.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_pic -> {
                    multiPartBody = null
                    viewModelPosts.setTypeAttach(AttachmentType.IMAGE)
                    ImagePicker.with(this)
                        .galleryOnly()
                        .crop()
                        .maxResultSize(2048, 2048)
                        .createIntent(launcher::launch)
                    true
                }

                R.id.add_geo -> {
                    val statusLoading =
                        viewModelPosts.newPostStatusModel.value?.statusLoading
                            ?: false
                    viewModelPosts.setStatusNewPostFrag(
                        NewPostStatusModel(
                            geo = SHOW,
                            statusLoading = statusLoading
                        )
                    )
                    true
                }

                R.id.add_users -> {
                    val statusCoords =
                        viewModelPosts.newPostStatusModel.value?.statusCoords ?: false
                    val statusLoading =
                        viewModelPosts.newPostStatusModel.value?.statusLoading
                            ?: false
                    viewModelPosts.setStatusNewPostFrag(
                        NewPostStatusModel(
                            groupUsers = SHOW,
                            statusCoords = statusCoords,
                            statusLoading = statusLoading
                        )
                    )
                    true
                }

                R.id.photo -> {
                    multiPartBody = null
                    viewModelPosts.setTypeAttach(null)
                    viewModelPosts.setTypeAttach(AttachmentType.IMAGE)
                    ImagePicker.with(this)
                        .cameraOnly()
                        .crop()
                        .maxResultSize(2048, 2048)
                        .createIntent(launcher::launch)
                    true
                }

                R.id.add_file -> {
                    multiPartBody?.let { "Выбранное вложение удалено!" }
                    multiPartBody = null
                    viewModelPosts.setTypeAttach(null)
                    val statusCoords =
                        viewModelPosts.newPostStatusModel.value?.statusCoords ?: false
                    viewModelPosts.setStatusNewPostFrag(
                        NewPostStatusModel(
                            groupLoadFile = SHOW,
                            statusCoords = statusCoords
                        )
                    )
                    true
                }

                else -> false
            }
        }


        with(binding) {

            attachAudio.setOnClickListener {
                viewModelPosts.setTypeAttach(AttachmentType.AUDIO)
                launcher.launch(getIntent())
            }
            attachVideo.setOnClickListener {
                viewModelPosts.setTypeAttach(AttachmentType.VIDEO)
                launcher.launch(getIntent())
            }
            btnClearLoading.setOnClickListener {
                val statusCoords = viewModelPosts.newPostStatusModel.value?.statusCoords ?: false
                viewModelPosts.setStatusNewPostFrag(NewPostStatusModel(statusCoords = statusCoords))
                multiPartBody = null
                viewModelPosts.setTypeAttach(null)
            }
        }

        binding.btnClear.setOnClickListener {
            viewModelPosts.clearPhoto()
            val statusCoords = viewModelPosts.newPostStatusModel.value?.statusCoords ?: false
            viewModelPosts.setStatusNewPostFrag(NewPostStatusModel(statusCoords = statusCoords))
            multiPartBody = null
            viewModelPosts.setTypeAttach(null)
        }

        viewModelPosts.photo.observe(viewLifecycleOwner) {
            if (it == viewModelPosts.noPhoto) {
                binding.content.focusAndShowKeyboard()
                return@observe
            }
            viewModelPosts.setTypeAttach(AttachmentType.IMAGE)
            binding.content.clearFocus()
            binding.photo.setImageURI(it.uri)
        }

        viewModelPosts.dataState.observe(viewLifecycleOwner) {
            if (it.loading) binding.btnClear.visibility = View.GONE
            if (!it.loading && lastStateLoading) findNavController().navigateUp()
            binding.progress.isVisible = it.loading
            lastStateLoading = it.loading
        }

        viewModelPosts.typeAttach.observe(viewLifecycleOwner) {}

        viewModelPosts.location.observe(viewLifecycleOwner) {
//            mapObjectCollection.clear()
            yakit.cleanMapObject()
            yakit.setMarkerInStartLocation(it)
            vibratePhone()
        }

        viewModelPosts.newPostStatusModel.observe(viewLifecycleOwner) { status ->
            with(binding) {

                groupImg.visibility = status.groupImage
                listUsers.visibility = status.groupUsers
                groupLoading.visibility = HIDE
                selectAttach.visibility = HIDE
                layMaps.visibility = status.geo
                if (layMaps.isVisible &&
                    viewModelPosts.newPostStatusModel.value?.statusLoading == true
                ) groupLoading.visibility = SHOW
                println("FILE ${viewModelPosts.typeAttach.value}")
                if (viewModelPosts.newPostStatusModel.value?.groupLoadFile == SHOW) {
                    if (viewModelPosts.newPostStatusModel.value?.statusLoading == false)
                        selectAttach.visibility = SHOW
                    else
                        groupLoading.visibility = SHOW
                    if (viewModelPosts.newPostStatusModel.value?.statusCoords == true) layMaps.visibility =
                        SHOW
                }

            }
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
        if (post == null) {
            curFrag?.getCurFragmentAttach(getString(R.string.new_post))
        } else {
            curFrag?.getCurFragmentAttach(getString(R.string.edit_post))
        }


    }


    private fun getIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*" // That's needed for some reason, crashes otherwise
        putExtra(
            // List all file types you want the user to be able to select
            Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg",
                "video/mp4",
            )
        )

    }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun Fragment.vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
    }

}