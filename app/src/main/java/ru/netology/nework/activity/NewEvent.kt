package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
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
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.eventArg
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.NewEventBinding
import ru.netology.nework.date.DateEvent
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.MeetingType
import ru.netology.nework.error.UnknownError
import ru.netology.nework.media.MediaModel
import ru.netology.nework.model.StatusModelViews
import ru.netology.nework.util.AndroidUtils.getFileName
import ru.netology.nework.util.AndroidUtils.getTime
import ru.netology.nework.util.AndroidUtils.getTimeFormat
import ru.netology.nework.viewmodel.EventsViewModel
import ru.netology.nework.viewmodel.LaysViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import java.util.Calendar
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewEvent : Fragment() {
    private val viewModelEvent: EventsViewModel by viewModels()
    private val viewModelUsers: UsersViewModel by viewModels()
    private val viewModelLays: LaysViewModel by viewModels()
    private var event: Event? = null
    private var dateAndTime = Calendar.getInstance()

    @Inject
    lateinit var yakit: YaKit


    private val users = mutableListOf<Long>()

    private val inputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {}

        override fun onMapLongTap(p0: Map, p1: Point) {
            viewModelLays.setLocation(Point(p1.latitude, p1.longitude))
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var lastStateLoading = false
        event = arguments?.eventArg
//        println("GET EVENT $event")

        val binding = NewEventBinding.inflate(layoutInflater)
        yakit.initMapView(binding.map)

        val startLocation = Point(55.75, 37.62)

        if (viewModelLays.location.value == null) {
            yakit.moveToStartLocation(startLocation)
            yakit.setMarkerInStartLocation(startLocation)
        } else {
            viewModelLays.location.value?.let { yakit.moveToStartLocation(it) }
            viewModelLays.location.value?.let { yakit.setMarkerInStartLocation(it) }
        }

        binding.map.map?.addInputListener(inputListener)

        if (event != null && viewModelLays.newStatusViewsModel.value!!.statusNewEvent) {
            viewModelLays.setStatusEdit()
            viewModelLays.setEvent(event!!)
            binding.content.setText(event?.content)
            viewModelLays.location.value?.let { yakit.moveToStartLocation(it) }
        }

        viewModelLays.listUsersEvent.value?.forEach { user ->
            users.add(user)
        }
        viewModelUsers.updateCheckableUsers(viewModelLays.listUsersEvent.value!!)

        val adapterUsers = AdapterUsersList(object : ListenerSelectionUser {
            override fun selectUser(user: UserResponse?) {

            }

            override fun addUser(idUser: Long?) {
                users.add(idUser!!)
                viewModelLays.changeListUsers(users)
            }

            override fun removeUser(idUser: Long?) {
                if (users.contains(idUser)) {
                    users.remove(idUser)
                    viewModelLays.changeListUsers(users)
                }
            }
        }, true)

        binding.listUsers.adapter = adapterUsers
        viewModelUsers.listUsers.observe(viewLifecycleOwner) { users ->
            adapterUsers.submitList(users)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        var multiPart: MultipartBody.Part? = null
                        if (viewModelLays.newStatusViewsModel.value?.groupUsers == SHOW) {
                            viewModelLays.setUsersGroup()
                        } else {

                            viewModelLays.mediaFile.value?.type?.let {
                                it.let { type ->
                                    if (type.contains(Regex("audio/"))) viewModelLays.setTypeAttach(
                                        AttachmentType.AUDIO
                                    )
                                    if (type.contains(Regex("video/"))) viewModelLays.setTypeAttach(
                                        AttachmentType.VIDEO
                                    )
                                }
                                println("MULTI $it")
                                multiPart = uploadStream(viewModelLays.mediaFile.value!!)
                            }

                            if (viewModelLays.typeAttach.value == AttachmentType.IMAGE
                                && viewModelLays.photo.value?.file != null
                            ) {
                                val photoModel = viewModelLays.photo.value
                                multiPart = MultipartBody.Part.createFormData(
                                    "file",
                                    photoModel?.file?.name,
                                    photoModel?.file!!.asRequestBody()
                                )
                            }
                            if (viewModelLays.typeAttach.value == null) {
                                viewModelLays.cleanAttach()
                                println("Attach Null")
                            }
                            val text = binding.content.text.toString()
                            val event = viewModelLays.getEvent(text)
                            if (event?.datetime == null) {
                                context?.toast("Необходимо установить дату и время события!")
                            } else {
                                viewModelEvent.saveEvent(
                                    event,
                                    multiPart,
                                    viewModelLays.typeAttach.value
                                )
                            }
//                            println("EVENT for send $event")
                        }
                        true
                    }

                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)

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
                    when (viewModelLays.typeAttach.value) {
                        AttachmentType.IMAGE -> {
                            viewModelLays.cleanMedia()
                            val file = uri?.toFile()
                            if (file?.length()!! < MAX_SIZE_FILE) {
                                viewModelLays.changePhoto(uri, file)
                                viewModelLays.setStatusLoadingImg(true)
                                viewModelLays.setImageGroup()
                            } else {
                                viewModelLays.setTypeAttach(null)
                                context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
                            }

                        }

                        AttachmentType.AUDIO -> {
                            viewModelLays.cleanPhoto()
                            it.data?.data?.let { _uri ->
                                val content = getContentLoading(_uri, "audio/")
                                content?.let { cont ->
                                    if (cont.length!! > MAX_SIZE_FILE) {
                                        context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
                                        return@registerForActivityResult
                                    }
                                    viewModelLays.changeMedia(cont)
                                    viewModelLays.setStatusLoadingFile(true)
                                    return@registerForActivityResult
                                }
                                viewModelLays.setTypeAttach(null)
                                context?.toast("Неправильный формат файла, загрузите аудио файл!")
                            }

                        }

                        AttachmentType.VIDEO -> {
                            viewModelLays.cleanPhoto()
                            it.data?.data?.let { _uri ->
                                val content = getContentLoading(_uri, "video/")
                                content?.let { cont ->
                                    if (cont.length!! > MAX_SIZE_FILE) {
                                        context?.toast("Размер вложения превышает максимально допустимый 15Мб!")
                                        return@registerForActivityResult
                                    }
                                    viewModelLays.changeMedia(cont)
                                    viewModelLays.setStatusLoadingFile(true)
                                    return@registerForActivityResult
                                }
                                viewModelLays.setTypeAttach(null)
                                context?.toast("Неправильный формат файла, загрузите видео файл!")
                            }
                        }

                        else -> {}
                    }
                }

                Activity.RESULT_CANCELED -> {

                }
            }
        }

        fun showViews(status: StatusModelViews) {
            with(binding) {
                groupImg.visibility = status.groupImage
                groupLoading.visibility = status.groupLoadFile
                selectAttach.visibility = status.groupSelectAttach
                layMaps.visibility = status.geo
                listUsers.visibility = status.groupUsers
                viewDate.visibility = status.groupDateEvent
                content.visibility = status.groupContent
            }

        }

        viewModelLays.newStatusViewsModel.observe(viewLifecycleOwner) { status ->
            showViews(status)
//            println("status $status, type ${viewModelLays.typeAttach.value}")
        }

        viewModelLays.photo.observe(viewLifecycleOwner) {
//            if (it == viewModelLays.noPhoto) {
//                binding.content.focusAndShowKeyboard()
//                return@observe
//            }

            binding.content.clearFocus()
            if (it.file == null) {
                Glide.with(binding.photo)
                    .load(it.uri)
                    .into(binding.photo)
            } else {
                binding.photo.setImageURI(it.uri)
            }
        }

        viewModelLays.mediaFile.observe(viewLifecycleOwner) {
            binding.content.clearFocus()
            when (viewModelLays.typeAttach.value) {
                AttachmentType.AUDIO -> {
                    Glide.with(binding.icAttach)
                        .load(R.drawable.audio_file_24)
                        .into(binding.icAttach)
                    binding.nameTrack.text = it.name
                }

                AttachmentType.VIDEO -> {
                    Glide.with(binding.icAttach)
                        .load(R.drawable.video_file_70)
                        .into(binding.icAttach)
                    binding.nameTrack.text = it.name
                    binding.nameTrack.text = it.name
                }

                else -> {}
            }
        }

        viewModelEvent.dataState.observe(viewLifecycleOwner) {
            if (it.loading) binding.btnClear.visibility = View.GONE
            if (!it.loading && lastStateLoading) findNavController().navigateUp()
            binding.progress.isVisible = it.loading
            lastStateLoading = it.loading
        }

        viewModelLays.typeAttach.observe(viewLifecycleOwner) {}
        viewModelLays.listUsersEvent.observe(viewLifecycleOwner) {}

        viewModelLays.location.observe(viewLifecycleOwner) {
            it?.let {
                yakit.cleanMapObject()
                yakit.setMarkerInStartLocation(it)
                vibratePhone()
            }
        }

        viewModelLays.dateEvent.observe(viewLifecycleOwner) {
            if (viewModelLays.dateEvent.value?.date == null) {
                viewModelLays.setDataTime(DateEvent(getTime(), null, MeetingType.ONLINE))
            }
            binding.currentDateTime.text = viewModelLays.dateEvent.value?.date
//            println("currentDateTime ${viewModelLays.dateEvent.value}")
        }

        binding.bottomNavigationNewEvent.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_pic -> {
                    //multiPartBody = null
                    if (viewModelLays.newStatusViewsModel.value?.statusLoadingImg == false) {
                        viewModelLays.setTypeAttach(AttachmentType.IMAGE)
                        ImagePicker.with(this)
                            .galleryOnly()
                            .crop()
                            .maxResultSize(2048, 2048)
                            .createIntent(launcher::launch)
                    } else viewModelLays.setImageGroup()

                    true
                }

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

                R.id.add_geo -> {
                    viewModelLays.setViewMaps()
                    true
                }

                R.id.add_users -> {
                    viewModelLays.setUsersGroup()
                    true
                }

                R.id.add_file -> {
//                    val uri = Uri.parse("")
//                    getFileName(uri, requireContext())
                    viewModelLays.setLoadingGroup()
                    true
                }

                R.id.add_date -> {
//                    binding.viewDate.visibility = SHOW
//                    binding.currentDateTime.text = getTime()
                    viewModelLays.setViewDateEvent()
                    true
                }

                else -> false
            }
        }

        with(binding) {

            attachAudio.setOnClickListener {
                if (viewModelLays.newStatusViewsModel.value?.statusLoadingFile == false) {
                    viewModelLays.setTypeAttach(AttachmentType.AUDIO)
                    launcher.launch(getIntent())
                } else viewModelLays.setLoadingGroup()
            }
            attachVideo.setOnClickListener {
                if (viewModelLays.newStatusViewsModel.value?.statusLoadingFile == false) {
                    viewModelLays.setTypeAttach(AttachmentType.VIDEO)
                    launcher.launch(getIntent())
                } else viewModelLays.setLoadingGroup()
            }
            btnClearLoading.setOnClickListener {
                cleanContent()
                viewModelLays.setStatusLoadingFile(false)
            }

            btnClear.setOnClickListener {
                cleanContent()
                viewModelLays.setLoadingGroup()
                //viewModelLays.setImageGroup()
            }

            content.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                        if (viewModelLays.newStatusViewsModel.value?.geo == SHOW)
                            viewModelLays.setViewMaps()
                        false
                    }

                    else -> {
                        true
                    }
                }
            }


            dateButton.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    datePickerDialog,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                ).show()
            }
            timeButton.setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    timePickerDialog,
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    true
                ).show()
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.btn_offline -> {
                        viewModelLays.setMeetingType(MeetingType.OFFLINE)
                    }

                    R.id.btn_online -> {
                        viewModelLays.setMeetingType(MeetingType.ONLINE)
                    }
                }
            }

        }

        return binding.root
    }


    @SuppressLint("Recycle")
    private fun getContentLoading(uri: Uri, typeFile: String): MediaModel? {
        val inputStream = context?.contentResolver?.openInputStream(uri)
        val type = context?.contentResolver?.getType(uri)
        return if (type != null && type.contains(Regex(typeFile))) {
            val length = inputStream?.available()
            val name = getFileName(uri, requireContext()) ?: "_"
            MediaModel(uri = uri, name = name, length = length, type = type)
        } else null
    }

    private fun cleanContent() {
        //multiPartBody = null
        viewModelLays.cleanPhoto()
        viewModelLays.cleanMedia()
        viewModelLays.setTypeAttach(null)
        viewModelLays.setStatusLoadingImg(false)

    }

    private fun getIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*" // That's needed for some reason, crashes otherwise
        putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg",
                "video/mp4",
            )
        )
    }

    fun uploadStream(mediaModel: MediaModel): MultipartBody.Part {
        val inputStream = mediaModel.uri?.let { context?.contentResolver?.openInputStream(it) }
        return MultipartBody.Part.createFormData(
            "file", mediaModel.name, RequestBody.create(
                mediaModel.type?.toMediaTypeOrNull(),
                inputStream!!.readBytes()
            )
        )
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
        yakit.stopMapView()
    }

    override fun onStart() {
        super.onStart()
        if (event == null) {
            curFrag?.getCurFragmentAttach(getString(R.string.new_event))
        } else {
            curFrag?.getCurFragmentAttach(getString(R.string.edit_event))
        }
    }

    private fun setInitialDateTime(): String {
        val dateTime = DateUtils.formatDateTime(
            context,
            dateAndTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_TIME
        )
        val td = getTimeFormat(dateAndTime)
        val date = td.subSequence(0, 10)
        val time = td.subSequence(11, 16)
        val timeForServer = "${date}T${time}:33.874Z"
        viewModelLays.setDataTime(DateEvent(dateTime, timeForServer))
        return dateTime
    }

    private var timePickerDialog =
        OnTimeSetListener { _, hourOfDay, minute ->
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            dateAndTime.set(Calendar.MINUTE, minute)
            setInitialDateTime()
        }

    // установка обработчика выбора даты
    private var datePickerDialog =
        OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            dateAndTime.set(Calendar.YEAR, year)
            dateAndTime.set(Calendar.MONTH, monthOfYear)
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setInitialDateTime()
        }

    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun Fragment.vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
