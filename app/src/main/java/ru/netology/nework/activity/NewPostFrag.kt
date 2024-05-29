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
import ru.netology.nework.model.StatusModelViews
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
                        viewModelPosts.newStatusViewsModel.value?.statusLoadingImg
                            ?: false
                    val statusCoords =
                        viewModelPosts.newStatusViewsModel.value?.statusCoords
                            ?: false
                    if (multiPartBody != null) {
                        viewModelPosts.setStatusViews(
                            StatusModelViews(
                                groupImage = SHOW,
                                statusCoords = statusCoords,
                                statusLoadingImg = statusLoading
                            )
                        )
                    } else {
                        StatusModelViews(
                            statusCoords = statusCoords,
                            statusLoadingImg = statusLoading
                        )
                    }
                }

                AttachmentType.AUDIO, AttachmentType.VIDEO -> {
                    val statusLoading =
                        viewModelPosts.newStatusViewsModel.value?.statusLoadingImg
                            ?: false
                    val statusCoords =
                        viewModelPosts.newStatusViewsModel.value?.statusCoords
                            ?: false
                    viewModelPosts.setStatusViews(
                        StatusModelViews(
                            groupLoadFile = SHOW,
                            statusCoords = statusCoords,
                            statusLoadingImg = statusLoading
                        )
                    )
                }

                else -> viewModelPosts.setStatusViews(StatusModelViews())
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
                                    viewModelPosts.newStatusViewsModel.value?.statusCoords ?: false
                                viewModelPosts.setStatusViews(
                                    StatusModelViews(
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
                                            viewModelPosts.newStatusViewsModel.value?.statusCoords
                                                ?: false
                                        viewModelPosts.setStatusViews(
                                            StatusModelViews(
                                                groupLoadFile = SHOW,
                                                statusLoadingImg = true,
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
                                            viewModelPosts.newStatusViewsModel.value?.statusCoords
                                                ?: false
                                        viewModelPosts.setStatusViews(
                                            StatusModelViews(
                                                groupLoadFile = SHOW,
                                                statusLoadingImg = true,
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
                        viewModelPosts.newStatusViewsModel.value?.statusLoadingImg
                            ?: false
                    viewModelPosts.setStatusViews(
                        StatusModelViews(
                            geo = SHOW,
                            statusLoadingImg = statusLoading
                        )
                    )
                    true
                }

                R.id.add_users -> {
                    val statusCoords =
                        viewModelPosts.newStatusViewsModel.value?.statusCoords ?: false
                    val statusLoading =
                        viewModelPosts.newStatusViewsModel.value?.statusLoadingImg
                            ?: false
                    viewModelPosts.setStatusViews(
                        StatusModelViews(
                            groupUsers = SHOW,
                            statusCoords = statusCoords,
                            statusLoadingImg = statusLoading
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
                        viewModelPosts.newStatusViewsModel.value?.statusCoords ?: false
                    viewModelPosts.setStatusViews(
                        StatusModelViews(
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
                val statusCoords = viewModelPosts.newStatusViewsModel.value?.statusCoords ?: false
                viewModelPosts.setStatusViews(StatusModelViews(statusCoords = statusCoords))
                multiPartBody = null
                viewModelPosts.setTypeAttach(null)
            }
        }

        binding.btnClear.setOnClickListener {
            viewModelPosts.clearPhoto()
            val statusCoords = viewModelPosts.newStatusViewsModel.value?.statusCoords ?: false
            viewModelPosts.setStatusViews(StatusModelViews(statusCoords = statusCoords))
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

        viewModelPosts.newStatusViewsModel.observe(viewLifecycleOwner) { status ->
            with(binding) {

                groupImg.visibility = status.groupImage
                listUsers.visibility = status.groupUsers
                groupLoading.visibility = HIDE
                selectAttach.visibility = HIDE
                layMaps.visibility = status.geo
                if (layMaps.isVisible &&
                    viewModelPosts.newStatusViewsModel.value?.statusLoadingImg == true
                ) groupLoading.visibility = SHOW
                println("FILE ${viewModelPosts.typeAttach.value}")
                if (viewModelPosts.newStatusViewsModel.value?.groupLoadFile == SHOW) {
                    if (viewModelPosts.newStatusViewsModel.value?.statusLoadingImg == false)
                        selectAttach.visibility = SHOW
                    else
                        groupLoading.visibility = SHOW
                    if (viewModelPosts.newStatusViewsModel.value?.statusCoords == true) layMaps.visibility =
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

//
//@OptIn(ExperimentalCoroutinesApi::class)
//@AndroidEntryPoint
//class NewEvent : Fragment() {
//    private val viewModelEvent: EventsViewModel by viewModels()
//    private val viewModelUsers: UsersViewModel by viewModels()
//    private val viewModelLays: LaysViewModel by viewModels()
//    private var event: Event? = null
//
//    @Inject
//    lateinit var yakit: YaKit
//
//
//    private val users = mutableListOf<Long>()
//
//    private val inputListener = object : InputListener {
//        override fun onMapTap(p0: Map, p1: Point) {}
//
//        override fun onMapLongTap(p0: Map, p1: Point) {
//            viewModelLays.setLocation(Point(p1.latitude, p1.longitude))
//        }
//    }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        var lastStateLoading = false
//        event = arguments?.eventArg
//        println("GET EVENT $event")
//        val binding = NewEventBinding.inflate(layoutInflater)
//        yakit.initMapView(binding.map)
//
//        val startLocation = Point(55.75, 37.62)
//
//        if (viewModelLays.location.value == null) {
//            yakit.moveToStartLocation(startLocation)
//            yakit.setMarkerInStartLocation(startLocation)
//        } else {
//            viewModelLays.location.value?.let { yakit.moveToStartLocation(it) }
//            viewModelLays.location.value?.let { yakit.setMarkerInStartLocation(it) }
//        }
//
//        binding.map.map?.addInputListener(inputListener)
//
//        if (event != null && viewModelLays.newStatusViewsModel.value!!.statusNewEvent) {
//            viewModelLays.setStatusEdit()
//            viewModelLays.setEvent(event!!)
//            binding.content.setText(event?.content)
//            viewModelLays.location.value?.let { yakit.moveToStartLocation(it) }
//        }
//
//        viewModelLays.listUsersEvent.value?.forEach { user ->
//            users.add(user)
//        }
//        viewModelUsers.updateCheckableUsers(viewModelLays.listUsersEvent.value!!)
//
//        val adapterUsers = AdapterUsersList(object : ListenerSelectionUser {
//            override fun selectUser(user: UserResponse?) {
//
//            }
//
//            override fun addUser(idUser: Long?) {
//                users.add(idUser!!)
//                viewModelLays.changeListUsers(users)
//            }
//
//            override fun removeUser(idUser: Long?) {
//                if (users.contains(idUser)) {
//                    users.remove(idUser)
//                    viewModelLays.changeListUsers(users)
//                }
//            }
//        }, true)
//
//        binding.listUsers.adapter = adapterUsers
//        viewModelUsers.listUsers.observe(viewLifecycleOwner) { users ->
//            adapterUsers.submitList(users)
//        }
//
//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.menu_save, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
//                when (menuItem.itemId) {
//                    R.id.save -> {
//                        var multiPart: MultipartBody.Part? = null
//                        if (viewModelLays.newStatusViewsModel.value?.groupUsers == SHOW) {
//                            viewModelLays.setUsersGroup()
//                        } else {
//
//                            viewModelLays.mediaFile.value?.type?.let {
//                                it.let { type ->
//                                    if (type.contains(Regex("audio/"))) viewModelLays.setTypeAttach(
//                                        AttachmentType.AUDIO
//                                    )
//                                    if (type.contains(Regex("video/"))) viewModelLays.setTypeAttach(
//                                        AttachmentType.VIDEO
//                                    )
//                                }
//                                println("MULTI $it")
//                                multiPart = uploadStream(viewModelLays.mediaFile.value!!)
//                            }
//
//                            if (viewModelLays.typeAttach.value == AttachmentType.IMAGE
//                                && viewModelLays.photo.value?.file != null
//                            ) {
//                                val photoModel = viewModelLays.photo.value
//                                multiPart = MultipartBody.Part.createFormData(
//                                    "file",
//                                    photoModel?.file?.name,
//                                    photoModel?.file!!.asRequestBody()
//                                )
//                            }
//                            if (viewModelLays.typeAttach.value == null) {
//                                viewModelLays.cleanAttach()
//                                println("Attach Null")
//                            }
//                            val text = binding.content.text.toString()
//                            val event = viewModelLays.getEvent(text)
//                            println("EVENT for send $event")
////                            viewModelEvent.saveEvent(
////                                event,
////                                multiPart,
////                                viewModelLays.typeAttach.value
////                            )
//                        }
//                        true
//                    }
//
//                    android.R.id.home -> {
//                        findNavController().navigateUp()
//                        true
//                    }
//
//                    else -> false
//                }
//
//        }, viewLifecycleOwner)
//
//        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            when (it.resultCode) {
//                ImagePicker.RESULT_ERROR -> {
//                    Snackbar.make(
//                        binding.root,
//                        ImagePicker.getError(it.data),
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                }
//
//                Activity.RESULT_OK -> {
//                    val uri: Uri? = it.data?.data
//                    when (viewModelLays.typeAttach.value) {
//                        AttachmentType.IMAGE -> {
//                            viewModelLays.cleanMedia()
//                            val file = uri?.toFile()
//                            if (file?.length()!! < MAX_SIZE_FILE) {
//                                viewModelLays.changePhoto(uri, file)
//                                viewModelLays.setStatusLoadingImg(true)
//                                viewModelLays.setImageGroup()
//                            } else {
//                                viewModelLays.setTypeAttach(null)
//                                context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
//                            }
//
//                        }
//
//                        AttachmentType.AUDIO -> {
//                            viewModelLays.cleanPhoto()
//                            it.data?.data?.let { _uri ->
//                                val content = getContentLoading(_uri, "audio/")
//                                content?.let { cont ->
//                                    if (cont.length!! > MAX_SIZE_FILE) {
//                                        context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
//                                        return@registerForActivityResult
//                                    }
//                                    viewModelLays.changeMedia(cont)
//                                    viewModelLays.setStatusLoadingFile(true)
//                                    return@registerForActivityResult
//                                }
//                                viewModelLays.setTypeAttach(null)
//                                context?.toast("Неправильный формат файла, загрузите аудио файл!")
//                            }
//
//                        }
//
//                        AttachmentType.VIDEO -> {
//                            viewModelLays.cleanPhoto()
//                            it.data?.data?.let { _uri ->
//                                val content = getContentLoading(_uri, "video/")
//                                content?.let { cont ->
//                                    if (cont.length!! > MAX_SIZE_FILE) {
//                                        context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
//                                        return@registerForActivityResult
//                                    }
//                                    viewModelLays.changeMedia(cont)
//                                    viewModelLays.setStatusLoadingFile(true)
//                                    return@registerForActivityResult
//                                }
//                                viewModelLays.setTypeAttach(null)
//                                context?.toast("Неправильный формат файла, загрузите видео файл!")
//                            }
//                        }
//
//                        else -> {}
//                    }
//                }
//
//                Activity.RESULT_CANCELED -> {
//
//                }
//            }
//        }
//
//        fun showViews(status: StatusViewsModel) {
//            with(binding) {
//                groupImg.visibility = status.groupImage
//                groupLoading.visibility = status.groupLoadFile
//                selectAttach.visibility = status.groupSelectAttach
//                layMaps.visibility = status.geo
//                listUsers.visibility = status.groupUsers
//            }
//
//        }
//
//        viewModelLays.newStatusViewsModel.observe(viewLifecycleOwner) { status ->
//            showViews(status)
////            println("status $status, type ${viewModelLays.typeAttach.value}")
//        }
//
//        viewModelLays.photo.observe(viewLifecycleOwner) {
////            if (it == viewModelLays.noPhoto) {
////                binding.content.focusAndShowKeyboard()
////                return@observe
////            }
//
//            binding.content.clearFocus()
//            if (it.file == null) {
//                Glide.with(binding.photo)
//                    .load(it.uri)
//                    .into(binding.photo)
//            } else {
//                binding.photo.setImageURI(it.uri)
//            }
//        }
//
//        viewModelLays.mediaFile.observe(viewLifecycleOwner) {
//            binding.content.clearFocus()
//            when (viewModelLays.typeAttach.value) {
//                AttachmentType.AUDIO -> {
//                    Glide.with(binding.icAttach)
//                        .load(R.drawable.audio_file_24)
//                        .into(binding.icAttach)
//                    binding.nameTrack.text = it.name
//                }
//
//                AttachmentType.VIDEO -> {
//                    Glide.with(binding.icAttach)
//                        .load(R.drawable.video_file_70)
//                        .into(binding.icAttach)
//                    binding.nameTrack.text = it.name
//                    binding.nameTrack.text = it.name
//                }
//
//                else -> {}
//            }
//        }
//
//        viewModelEvent.dataState.observe(viewLifecycleOwner) {
//            if (it.loading) binding.btnClear.visibility = View.GONE
//            if (!it.loading && lastStateLoading) findNavController().navigateUp()
//            binding.progress.isVisible = it.loading
//            lastStateLoading = it.loading
//        }
//
//        viewModelLays.typeAttach.observe(viewLifecycleOwner) {}
//        viewModelLays.listUsersEvent.observe(viewLifecycleOwner) {}
//
//        viewModelLays.location.observe(viewLifecycleOwner) {
//            it?.let {
//                yakit.cleanMapObject()
//                yakit.setMarkerInStartLocation(it)
//                vibratePhone()
//            }
//        }
//
//        binding.bottomNavigationNewEvent.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.add_pic -> {
//                    //multiPartBody = null
//                    if (viewModelLays.newStatusViewsModel.value?.statusLoadingImg == false) {
//                        viewModelLays.setTypeAttach(AttachmentType.IMAGE)
//                        ImagePicker.with(this)
//                            .galleryOnly()
//                            .crop()
//                            .maxResultSize(2048, 2048)
//                            .createIntent(launcher::launch)
//                    } else viewModelLays.setImageGroup()
//
//                    true
//                }
//
//                R.id.photo -> {
//                    if (viewModelLays.newStatusViewsModel.value?.statusLoadingImg == false) {
//                        //multiPartBody = null
//                        viewModelLays.setTypeAttach(AttachmentType.IMAGE)
//                        ImagePicker.with(this)
//                            .cameraOnly()
//                            .crop()
//                            .maxResultSize(2048, 2048)
//                            .createIntent(launcher::launch)
//                    } else viewModelLays.setImageGroup()
//
//                    true
//                }
//
//                R.id.add_geo -> {
//                    viewModelLays.setViewMaps()
//                    true
//                }
//
//                R.id.add_users -> {
//                    viewModelLays.setUsersGroup()
//                    true
//                }
//
//                R.id.add_file -> {
////                    val uri = Uri.parse("")
////                    getFileName(uri, requireContext())
//                    viewModelLays.setLoadingGroup()
//                    true
//                }
//
//                else -> false
//            }
//        }
//
//        with(binding) {
//
//            attachAudio.setOnClickListener {
//                if (viewModelLays.newStatusViewsModel.value?.statusLoadingFile == false) {
//                    viewModelLays.setTypeAttach(AttachmentType.AUDIO)
//                    launcher.launch(getIntent())
//                } else viewModelLays.setLoadingGroup()
//            }
//            attachVideo.setOnClickListener {
//                if (viewModelLays.newStatusViewsModel.value?.statusLoadingFile == false) {
//                    viewModelLays.setTypeAttach(AttachmentType.VIDEO)
//                    launcher.launch(getIntent())
//                } else viewModelLays.setLoadingGroup()
//            }
//            btnClearLoading.setOnClickListener {
//                cleanContent()
//                viewModelLays.setStatusLoadingFile(false)
//            }
//
//            btnClear.setOnClickListener {
//                cleanContent()
//                viewModelLays.setLoadingGroup()
//                //viewModelLays.setImageGroup()
//            }
//content.setOnTouchListener { v, event ->
//    when(event.action){
//        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
//            if(viewModelLays.newStatusViewsModel.value?.geo == SHOW)
//                viewModelLays.setViewMaps()
//            false
//        }
//        else -> {
//            true
//        }
//    }
//}
//        }
//
//
//        return binding.root
//    }
//
//
//    @SuppressLint("Recycle")
//    private fun getContentLoading(uri: Uri, typeFile: String): MediaModel? {
//        val inputStream = context?.contentResolver?.openInputStream(uri)
//        val type = context?.contentResolver?.getType(uri)
//        return if (type != null && type.contains(Regex(typeFile))) {
//            val length = inputStream?.available()
//            val name = AndroidUtils.getFileName(uri, requireContext()) ?: "_"
//            MediaModel(uri = uri, name = name, length = length, type = type)
//        } else null
//    }
//
//    private fun cleanContent() {
//        //multiPartBody = null
//        viewModelLays.cleanPhoto()
//        viewModelLays.cleanMedia()
//        viewModelLays.setTypeAttach(null)
//        viewModelLays.setStatusLoadingImg(false)
//
//    }
//
//    private fun getIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//        addCategory(Intent.CATEGORY_OPENABLE)
//        type = "*/*" // That's needed for some reason, crashes otherwise
//        putExtra(
//            Intent.EXTRA_MIME_TYPES, arrayOf(
//                "audio/mpeg",
//                "video/mp4",
//            )
//        )
//    }
//
//    fun uploadStream(mediaModel: MediaModel): MultipartBody.Part {
//        val inputStream = mediaModel.uri?.let { context?.contentResolver?.openInputStream(it) }
//        return MultipartBody.Part.createFormData(
//            "file", mediaModel.name, RequestBody.create(
//                mediaModel.type?.toMediaTypeOrNull(),
//                inputStream!!.readBytes()
//            )
//        )
//    }
//
//    private var curFrag: CurrentShowFragment? = null
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            curFrag = context as CurrentShowFragment
//        } catch (e: ClassCastException) {
//            throw UnknownError
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        curFrag?.getCurFragmentDetach()
//        curFrag = null
//        yakit.stopMapView()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        if (event == null) {
//            curFrag?.getCurFragmentAttach(getString(R.string.new_event))
//        } else {
//            curFrag?.getCurFragmentAttach(getString(R.string.edit_event))
//        }
//    }
//
//    private fun Context.toast(message: CharSequence) =
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//
//    private fun Fragment.vibratePhone() {
//        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
//    }
//}