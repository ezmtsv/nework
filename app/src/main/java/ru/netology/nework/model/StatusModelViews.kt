package ru.netology.nework.model

import android.view.View

data class StatusModelViews(
    val groupUsers: Int = View.GONE,
    val groupImage: Int = View.GONE,
    val groupLoadFile: Int = View.GONE,
    val groupSelectAttach: Int = View.GONE,
    val groupDateEvent: Int = View.GONE,
    val groupContent: Int = View.VISIBLE,
    val geo: Int = View.GONE,
    val statusCoords: Boolean = false,
    val statusLoadingImg: Boolean = false,
    val statusLoadingFile: Boolean = false,
    val statusViewImage: Boolean = false,
    val statusViewUsers: Boolean = false,
    val statusViewMaps: Boolean = false,
    val statusViewLoading: Boolean = false,
    val statusNewEvent: Boolean = true,
    val statusDateEvent: Boolean = false,
    val statusNewPost: Boolean = true,
)