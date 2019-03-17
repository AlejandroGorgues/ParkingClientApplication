package com.example.parkingclientapplication.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.parkingclientapplication.R
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import android.widget.Button
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import android.graphics.BitmapFactory
import android.widget.Toast
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class MapDirectionFragment : Fragment(), OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    // variables for adding location layer
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationComponent: LocationComponent
    // variables for calculating and drawing a route
    private lateinit var currentRoute: DirectionsRoute
    private val TAG = "DirectionsActivity"
    private var navigationMapRoute: NavigationMapRoute? = null
    // variables needed to initialize navigation
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Mapbox.getInstance(context!!, getString(R.string.access_token))
        val view = inflater.inflate(R.layout.fragment_map_direction, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        // Inflate the layout for this fragment
        return view
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day)
        ) { style ->
            enableLocationComponent(style)

            addDestinationIconSymbolLayer(style)

            mapboxMap.addOnMapClickListener(this@MapDirectionFragment)
            button = view!!.findViewById(R.id.startButton)
            button.setOnClickListener {
                val simulateRoute = true
                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simulateRoute)
                    .build()
                // Call this method with Context from within an Activity
                NavigationLauncher.startNavigation(activity, options)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapClick(point: LatLng): Boolean {
        val destinationPoint = Point.fromLngLat(point.longitude, point.latitude)
        val originPoint = Point.fromLngLat(locationComponent.lastKnownLocation!!.longitude,
            locationComponent.lastKnownLocation!!.latitude
        )

        mapboxMap.style!!.getSourceAs<GeoJsonSource>("destination-source-id")?.setGeoJson(Feature.fromGeometry(destinationPoint))
        getRoute(originPoint, destinationPoint)
        button.isEnabled = true
        button.setBackgroundResource(R.color.blueButton)
        return true
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(context, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            activity!!.finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<String>, grantResults:IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onSaveInstanceState(outState:Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun addDestinationIconSymbolLayer(loadedMapStyle: Style) {
        loadedMapStyle.addImage(
            "destination-icon-id",
            BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default)
        )
        val geoJsonSource = GeoJsonSource("destination-source-id")
        loadedMapStyle.addSource(geoJsonSource)
        val destinationSymbolLayer = SymbolLayer("destination-symbol-layer-id", "destination-source-id")
        destinationSymbolLayer.withProperties(
            iconImage("destination-icon-id"),
            iconAllowOverlap(true),
            iconIgnorePlacement(true)
        )
        loadedMapStyle.addLayer(destinationSymbolLayer)
    }

    private fun getRoute(origin:Point, destination:Point) {
        NavigationRoute.builder(context)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object: Callback<DirectionsResponse> {
                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                    // You can get the generic HTTP info about the response
                    Timber.d("Response code: %s", response.code())
                    if (response.body() == null)
                    {
                        Timber.e("No routes found, make sure you set the right user and access token.")
                        return
                    }
                    else if (response.body()!!.routes().size < 1)
                    {
                        Timber.e("No routes found")
                        return
                    }
                    currentRoute = response.body()!!.routes()[0]
                    // Draw the route on the map
                    if (navigationMapRoute != null)
                    {
                        navigationMapRoute!!.updateRouteArrowVisibilityTo(false)
                        navigationMapRoute!!.updateRouteVisibilityTo(false)
                    }
                    else
                    {
                        navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute)
                    }
                    navigationMapRoute!!.addRoute(currentRoute)
                }
                override fun onFailure(call:Call<DirectionsResponse>, throwable:Throwable) {
                    Timber.e("Error: %s", throwable.message)
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle:Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context))
        {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(context!!, loadedMapStyle)
            locationComponent.isLocationComponentEnabled = true
            // Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING
        }
        else
        {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }






}
