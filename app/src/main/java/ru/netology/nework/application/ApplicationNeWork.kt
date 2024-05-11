package ru.netology.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class ApplicationNeWork: Application() {
    init {
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}