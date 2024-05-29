package ru.netology.nework.date

import ru.netology.nework.enumeration.MeetingType

data class DateEvent (
    val date: String? = null,
    val dateForSending:String? = null,
    val meetingType: MeetingType = MeetingType.ONLINE
)