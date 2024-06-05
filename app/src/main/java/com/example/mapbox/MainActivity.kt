package com.example.mapbox

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.testapp.utils.LocationPermissionHelper
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = object : OnIndicatorBearingChangedListener {
        override fun onIndicatorBearingChanged(bearing: Double) {
            //mapView.mapboxMap.setCamera(CameraOptions.Builder().bearing(bearing).build())
        }
    }

    private val onIndicatorPositionChangedListener = object : OnIndicatorPositionChangedListener {
        override fun onIndicatorPositionChanged(point: Point) {
            // mapView.mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
            // mapView.gestures.focalPoint = mapView.mapboxMap.pixelForCoordinate(point)
        }
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean = false

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var mapView: MapView

    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        //setContentView(R.layout.activity_main)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions { onMapReady() }
    }

    private fun onMapReady() {
        mapView.getMapboxMap()?.apply { // Safe access using ?.apply
            //mapboxMap = this // Assign mapboxMap after it's initialized
            setCamera(
                CameraOptions.Builder().zoom(14.0).build()
            )
            loadStyle(Style.STANDARD) {
                initLocationComponent()
                setupGesturesListener()

                // Access mapboxMap here (now initialized)
            }
        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            puckBearing = PuckBearing.COURSE
            puckBearingEnabled = true
            enabled = true
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.baseline_location_on_24),
                shadowImage = ImageHolder.from(R.drawable.baseline_my_location_24),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}