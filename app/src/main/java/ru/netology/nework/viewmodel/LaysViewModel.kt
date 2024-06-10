package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.activity.HIDE
import ru.netology.nework.activity.SHOW
import ru.netology.nework.date.DateEvent
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.MeetingType
import ru.netology.nework.media.MediaModel
import ru.netology.nework.media.PhotoModel
import ru.netology.nework.model.StatusModelViews
import ru.netology.nework.util.AndroidUtils.getTimePublish
import java.io.File
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LaysViewModel @Inject constructor(

) : ViewModel() {

    private val _location = MutableLiveData<Point>()
    val location: LiveData<Point>
        get() = _location

    private val _newStatusViewsModel = MutableLiveData(StatusModelViews())
    val newStatusViewsModel: LiveData<StatusModelViews>
        get() = _newStatusViewsModel

    private val _typeAttach = MutableLiveData<AttachmentType?>()
    val typeAttach: LiveData<AttachmentType?>
        get() = _typeAttach

    private val noPhoto = PhotoModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _mediaFile = MutableLiveData(MediaModel())
    val mediaFile: LiveData<MediaModel>
        get() = _mediaFile

    private val _listUsersEvent = MutableLiveData(listOf<Long>())
    val listUsersEvent: LiveData<List<Long>>
        get() = _listUsersEvent

    private val _listUsersMentions = MutableLiveData(listOf<Long>())
    val listUsersMentions: LiveData<List<Long>>
        get() = _listUsersMentions

    private val _dateEvent = MutableLiveData(DateEvent())
    val dateEvent: LiveData<DateEvent>
        get() = _dateEvent

    private val _event = MutableLiveData(Event(id = 0L))
    private val _post = MutableLiveData(Post(id = 0L, authorId = 0L))
//    val event: LiveData<Event>
//        get() = _event

    fun setLocation(point: Point) {
        _location.value = point
    }

    fun setTypeAttach(attach: AttachmentType?) {
        _typeAttach.value = attach
    }

    fun setStatusLoadingImg(statusLoading: Boolean) {
        val status = _newStatusViewsModel.value
        _newStatusViewsModel.value = status?.copy(statusLoadingImg = statusLoading)
    }

    fun setStatusLoadingFile(statusLoading: Boolean) {
//        val status = _newStatusViewsModel.value
//        if (statusLoading) {
//            _newStatusViewsModel.value =
//                status?.copy(statusLoadingFile = statusLoading, statusViewLoading = false)
//        } else {
//            _newStatusViewsModel.value =
//                status?.copy(statusLoadingFile = statusLoading)
//        }
        val status = _newStatusViewsModel.value
        _newStatusViewsModel.value =
            status?.copy(statusLoadingFile = statusLoading)
        if (statusLoading) {
            _newStatusViewsModel.value = _newStatusViewsModel.value?.copy(
                statusViewLoading = false
            )
        }
        setLoadingGroup()
    }

    fun setImageGroup() {
        val status = _newStatusViewsModel.value
        val statusView = _newStatusViewsModel.value?.statusViewImage!!
        if (!statusView) _newStatusViewsModel.value = status?.copy(
            groupImage = SHOW,
            groupContent = SHOW,
            groupLoadFile = HIDE,
            groupUsers = HIDE,
            groupSelectAttach = HIDE,
            geo = HIDE,
            groupDateEvent = HIDE,
            statusViewImage = !_newStatusViewsModel.value?.statusViewImage!!,
            statusViewLoading = false,
            statusViewUsers = false,
            statusViewMaps = false,
            statusDateEvent = false,
        )
        else _newStatusViewsModel.value = status?.copy(
            groupImage = HIDE,
            groupLoadFile = HIDE,
            groupUsers = HIDE,
            groupSelectAttach = HIDE,
            geo = HIDE,
            statusViewImage = !_newStatusViewsModel.value?.statusViewImage!!,
            statusViewLoading = false,
            statusViewUsers = false,
            statusViewMaps = false,
        )
    }

    fun setLoadingGroup() {
        val status = _newStatusViewsModel.value
        val statusView = _newStatusViewsModel.value?.statusViewLoading!!
        if (!statusView) {
            if (_newStatusViewsModel.value!!.statusLoadingFile) {
                _newStatusViewsModel.value = status?.copy(
                    groupImage = HIDE,
                    groupContent = SHOW,
                    groupLoadFile = SHOW,
                    groupSelectAttach = HIDE,
                    groupUsers = HIDE,
                    geo = _newStatusViewsModel.value?.geo!!,
                    statusViewLoading = !_newStatusViewsModel.value?.statusViewLoading!!,
                    statusViewImage = false,
                    statusViewUsers = false,
                )
            } else {
                _newStatusViewsModel.value = status?.copy(
                    groupImage = HIDE,
                    groupContent = SHOW,
                    groupLoadFile = HIDE,
                    groupSelectAttach = SHOW,
                    groupUsers = HIDE,
                    geo = _newStatusViewsModel.value?.geo!!,
                    statusViewLoading = !_newStatusViewsModel.value?.statusViewLoading!!,
                    statusViewImage = false,
                    statusViewUsers = false,
                )
            }
        } else _newStatusViewsModel.value = status?.copy(
            groupImage = HIDE,
            groupLoadFile = HIDE,
            groupSelectAttach = HIDE,
            groupUsers = HIDE,
            geo = _newStatusViewsModel.value?.geo!!,
            statusViewLoading = !_newStatusViewsModel.value?.statusViewLoading!!,
            statusViewImage = false,
            statusViewUsers = false,
        )
    }


    fun setUsersGroup() {
        val status = _newStatusViewsModel.value
        val statusView = _newStatusViewsModel.value?.statusViewUsers!!


        if (!statusView) {
            _newStatusViewsModel.value = status?.copy(
                groupImage = HIDE,
                groupLoadFile = HIDE,
                groupSelectAttach = HIDE,
                geo = HIDE,
                groupDateEvent = HIDE,
                groupContent = HIDE,
                groupUsers = SHOW,
                statusViewImage = false,
                statusDateEvent = false,
                statusViewMaps = false,
                statusViewLoading = false,
                statusViewUsers = !_newStatusViewsModel.value?.statusViewUsers!!,
            )
        } else {
            _newStatusViewsModel.value = status?.copy(
                statusViewUsers = !_newStatusViewsModel.value?.statusViewUsers!!,
                groupContent = SHOW,
                groupUsers = HIDE
            )
        }
    }


    fun setViewMaps() {
        val status = _newStatusViewsModel.value
        val statusView = _newStatusViewsModel.value?.statusViewMaps!!
        if (!statusView) {
            _newStatusViewsModel.value = status?.copy(
                groupImage = HIDE,
                groupUsers = HIDE,
                groupDateEvent = HIDE,
                groupContent = HIDE,
                geo = SHOW,
                statusViewMaps = !_newStatusViewsModel.value?.statusViewMaps!!,
                statusViewImage = false,
                statusViewUsers = false,
                statusDateEvent = false,
            )

        } else {
            _newStatusViewsModel.value = status?.copy(
                statusViewMaps = !_newStatusViewsModel.value?.statusViewMaps!!,
                groupContent = SHOW,
                geo = HIDE
            )

        }
    }

    fun setViewDateEvent() {
        val status = _newStatusViewsModel.value
        val statusView = _newStatusViewsModel.value?.statusDateEvent!!
        if (!statusView) {
            _newStatusViewsModel.value = status?.copy(
                groupImage = HIDE,
                groupUsers = HIDE,
                geo = HIDE,
                groupDateEvent = SHOW,
                groupContent = HIDE,
                statusDateEvent = !_newStatusViewsModel.value?.statusDateEvent!!,
                statusViewMaps = false,
                statusViewImage = false,
                statusViewUsers = false,
            )
        } else {
            _newStatusViewsModel.value = status?.copy(
                statusDateEvent = !_newStatusViewsModel.value?.statusDateEvent!!,
                groupContent = SHOW,
                groupDateEvent = HIDE
            )
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun cleanPhoto() {
        setStatusLoadingImg(false)
        _photo.value = noPhoto
    }

    fun cleanMedia() {
        setStatusLoadingFile(false)
        _mediaFile.value = MediaModel()
    }

    fun changeMedia(content: MediaModel) {
        _mediaFile.value = content
    }

    fun changeListUsers(list: List<Long>) {
        _listUsersEvent.value = list
        _listUsersMentions.value = list
    }


    fun setEvent(event: Event) {
        _event.value = event

        event.coords?.let {
            val lat = event.coords.lat!!
            val longCr = event.coords.longCr!!
            _location.value = Point(lat, longCr)
        }

        _listUsersEvent.value = event.speakerIds
        event.attachment?.let {
            _typeAttach.value = it.type
            when (it.type) {
                AttachmentType.IMAGE -> {
                    _photo.value = PhotoModel(uri = Uri.parse(event.attachment.url))
                    setStatusLoadingImg(true)
                    setTypeAttach(AttachmentType.IMAGE)
                    setImageGroup()
                }

                AttachmentType.VIDEO -> {
                    _mediaFile.value =
                        MediaModel(uri = Uri.parse(event.attachment.url), event.attachment.url, 0)
                    setTypeAttach(AttachmentType.VIDEO)
                    setStatusLoadingFile(true)
                }

                AttachmentType.AUDIO -> {
                    _mediaFile.value =
                        MediaModel(uri = Uri.parse(event.attachment.url), event.attachment.url, 0)
                    setTypeAttach(AttachmentType.AUDIO)
                    setStatusLoadingFile(true)
                }

                null -> {}
            }
        }
        event.typeMeeting?.let {
            setMeetingType(it)
        }
        val date = getTimePublish(event.datetime)
        setDataTime(DateEvent(date = date, dateForSending = event.datetime))

    }

    fun setPost(post: Post) {
        _post.value = post

        post.coords?.let {
            val lat = post.coords.lat!!
            val longCr = post.coords.longCr!!
            _location.value = Point(lat, longCr)
        }

        _listUsersMentions.value = post.mentionIds
        post.attachment?.let {
            _typeAttach.value = it.type
            when (it.type) {
                AttachmentType.IMAGE -> {
                    _photo.value = PhotoModel(uri = Uri.parse(post.attachment.url))
                    setStatusLoadingImg(true)
                    setTypeAttach(AttachmentType.IMAGE)
                    setImageGroup()
                }

                AttachmentType.VIDEO -> {
                    _mediaFile.value =
                        MediaModel(uri = Uri.parse(post.attachment.url), post.attachment.url, 0)
                    setTypeAttach(AttachmentType.VIDEO)
                    setStatusLoadingFile(true)
                }

                AttachmentType.AUDIO -> {
                    _mediaFile.value =
                        MediaModel(uri = Uri.parse(post.attachment.url), post.attachment.url, 0)
                    setTypeAttach(AttachmentType.AUDIO)
                    setStatusLoadingFile(true)
                }

                null -> {}
            }
        }
    }

    fun cleanAttach() {
        _event.value =
            _event.value?.copy(attachment = null)
    }

    fun setDataTime(date: DateEvent) {
        _dateEvent.value =
            _dateEvent.value?.copy(date = date.date, dateForSending = date.dateForSending)
    }

    fun setMeetingType(type: MeetingType) {
        _dateEvent.value = _dateEvent.value?.copy(meetingType = type)
    }

    fun setStatusEdit() {
        _newStatusViewsModel.value = StatusModelViews(statusNewEvent = false, statusNewPost = false)
    }

    fun getEvent(text: String): Event? {
        _event.value =
            _event.value?.copy(
                content = text,
                coords = Coordinates(_location.value?.latitude, _location.value?.longitude),
                speakerIds = listUsersEvent.value,
                typeMeeting = _dateEvent.value?.meetingType,
                datetime = _dateEvent.value?.dateForSending,
                eventOwner = true,
            )

       return _event.value
    }

    fun getPost(text: String): Post? {
        _post.value =
            _post.value?.copy(
                content = text,
                coords = Coordinates(_location.value?.latitude, _location.value?.longitude),
                mentionIds = listUsersMentions.value,
                postOwner = true,
            )
        return _post.value
    }

}
