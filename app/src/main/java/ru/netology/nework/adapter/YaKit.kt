package ru.netology.nework.adapter

import android.content.Context
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nework.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YaKit @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    companion object YKit {
        var mapView: MapView? = null
        var yandexMapsKitFactory: MapKit? = null
        lateinit var mapObjectCollection: MapObjectCollection
        lateinit var placemarkMapObject: PlacemarkMapObject
        var zoomValue: Float = 16.5f
    }

    fun initMapView(view: MapView){
        mapView = view
        yandexMapsKitFactory = MapKitFactory.getInstance()
        yandexMapsKitFactory?.onStart()
        mapView?.onStart()
    }

    @Suppress("DEPRECATION")
    fun moveToStartLocation(point: Point) {
        mapView?.map?.move(
            CameraPosition(point, zoomValue, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
    }

    @Suppress("DEPRECATION")
    fun setMarkerInStartLocation(startLocation: Point) {
        val marker = R.drawable.ic_pin_black_png // Добавляем ссылку на картинку
        mapObjectCollection =
            mapView?.map!!.mapObjects // Инициализируем коллекцию различных объектов на карте
        placemarkMapObject = mapObjectCollection.addPlacemark(
            startLocation,
            ImageProvider.fromResource(context, marker)
        ) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.5f // Устанавливаем прозрачность метке
        placemarkMapObject.setText("Здесь!") // Устанавливаем текст сверху метки
    }

    fun cleanMapObject(){
        mapObjectCollection.clear()
    }

    fun stopMapView(){
        mapView?.onStop()
        MapKitFactory.getInstance()?.onStop()
    }
}