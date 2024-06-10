package ru.netology.nework.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object AndroidUtils {

//    fun View.focusAndShowKeyboard() {
//        fun View.showTheKeyboardNow() {
//            if (isFocused) {
//                post {
//                    // We still post the call, just in case we are being notified of the windows focus
//                    // but InputMethodManager didn't get properly setup yet.
//                    val imm =
//                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
//                }
//            }
//        }
//
//        requestFocus()
//        if (hasWindowFocus()) {
//            // No need to wait for the window to get focus.
//            showTheKeyboardNow()
//        } else {
//            // We need to wait until the window gets focus.
//            viewTreeObserver.addOnWindowFocusChangeListener(
//                object : ViewTreeObserver.OnWindowFocusChangeListener {
//                    override fun onWindowFocusChanged(hasFocus: Boolean) {
//                        // This notification will arrive just before the InputMethodManager gets set up.
//                        if (hasFocus) {
//                            this@focusAndShowKeyboard.showTheKeyboardNow()
//                            // It’s very important to remove this listener once we are done.
//                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
//                        }
//                    }
//                })
//        }
//    }

    fun getTimePublish(str: String?): String {
        //"2023-10-17T13:01:59.846Z"
        //"2024-04-25T15:00:33.874Z"
        str?.let{
            try {
                val yyyy = it.subSequence(0, 4)
                val mm = it.subSequence(5, 7)
                val dd = it.subSequence(8, 10)
                val hh = it.subSequence(11, 13)
                val min = it.subSequence(14, 16)
                return "$dd.$mm.$yyyy  $hh:$min"
            } catch (e: Exception) {
                println(e.printStackTrace())
            }
        }
        return " "
    }

    fun getTimeJob(str: String?): String{
        str?.let{
            try {
                val yyyy = it.subSequence(0, 4)
                val mm = it.subSequence(5, 7)
                val dd = it.subSequence(8, 10)
                return "$dd.$mm.$yyyy"
            } catch (e: Exception) {
                println(e.printStackTrace())
            }
        }
        return "НВ"
    }

    fun getTimeTrack(time: Int): String =
        if (time == 0) {
            "....."
        } else {
            "  ${time / 1000 / 60}:${time / 1000 % 60} мин."
        }

    fun getFileName(uri: Uri, context: Context): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor = context.contentResolver?.query(uri, null, null, null, null)
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

    @SuppressLint("SimpleDateFormat")
    fun getTime(): String =
        SimpleDateFormat("dd MMMM yyyy в HH:mm").format(Calendar.getInstance().time).toString()
    @SuppressLint("SimpleDateFormat")
    fun getDate(): String =
        SimpleDateFormat("dd MMMM yyyy").format(Calendar.getInstance().time).toString()
    @SuppressLint("SimpleDateFormat")
    fun getDate(date: Calendar): String =
        SimpleDateFormat("dd MMMM yyyy").format(date.time).toString()
    @SuppressLint("SimpleDateFormat")
    fun getTimeFormat(date: Calendar): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm").format(date.time).toString()
    //"2024-04-25T15:00:33.874Z"

}

object StringArg : ReadWriteProperty<Bundle, String?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: String?) {
        thisRef.putString(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): String? =
        thisRef.getString(property.name)
}

object LongEditArg : ReadWriteProperty<Bundle, Long> {
    override fun getValue(thisRef: Bundle, property: KProperty<*>): Long =
        thisRef.getLong(property.name)

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Long) {
        thisRef.putLong(property.name, value)
    }
}

object UserArg : ReadWriteProperty<Bundle, UserResponse?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: UserResponse?) {
        thisRef.putString(property.name, Gson().toJson(value))
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): UserResponse? {
        val listType: Type = object : TypeToken<UserResponse?>() {}.type
        return Gson().fromJson<UserResponse?>(thisRef.getString(property.name), listType)
    }
}

object ListUserArg : ReadWriteProperty<Bundle, List<UserResponse>?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: List<UserResponse>?) {
        thisRef.putString(property.name, Gson().toJson(value))
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): List<UserResponse>? {
        val listType: Type = object : TypeToken<List<UserResponse>?>() {}.type
        return Gson().fromJson<List<UserResponse>?>(thisRef.getString(property.name), listType)
    }
}

object PostArg : ReadWriteProperty<Bundle, Post?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Post?) {
        thisRef.putString(property.name, Gson().toJson(value))
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Post? {
        val type: Type = object : TypeToken<Post?>() {}.type
        return Gson().fromJson<Post?>(thisRef.getString(property.name), type)
    }
}

object EventArg : ReadWriteProperty<Bundle, Event?> {

    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Event?) {
        thisRef.putString(property.name, Gson().toJson(value))
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Event? {
        val type: Type = object : TypeToken<Event?>() {}.type
        return Gson().fromJson<Event?>(thisRef.getString(property.name), type)
    }
}