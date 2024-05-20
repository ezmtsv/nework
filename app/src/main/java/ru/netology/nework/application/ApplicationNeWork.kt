package ru.netology.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.netology.nework.BuildConfig

@HiltAndroidApp
class ApplicationNeWork: Application() {
    init {
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}