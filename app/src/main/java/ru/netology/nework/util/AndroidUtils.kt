package ru.netology.nework.util

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import java.lang.reflect.Type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object AndroidUtils {

    fun View.focusAndShowKeyboard() {
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }

    fun getTimePublish(str: String?): String {
        //"2023-10-17T13:01:59.846Z"
        str?.let{
            var dd = ""
            var mm = ""
            var yyyy= ""
            var hh = ""
            var min = ""
            var count = 0
            for(i in str) {
                if(count < 4) yyyy+= i
                else if (count<7 && i != '-')mm+=i
                else if(count<10 && i != '-')dd+=i
                else if(count in 11..12)hh+=i
                else if(count in 14..15)min+=i
                count++
            }
            return "$dd.$mm.$yyyy  $hh:$min"
        }
        return " "
    }

    fun getTimeJob(str: String?): String{
        str?.let{
            var dd = ""
            var mm = ""
            var yyyy= ""
            var count = 0
            for(i in str) {
                if(count < 4) yyyy+= i
                else if (count<7 && i != '-')mm+=i
                else if(count<10 && i != '-')dd+=i
                else if(count >11) break
                count++
            }
            return "$dd.$mm.$yyyy"
        }
        return "НВ"
    }

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