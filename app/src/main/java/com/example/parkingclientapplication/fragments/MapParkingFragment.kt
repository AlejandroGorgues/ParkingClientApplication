package com.example.parkingclientapplication.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import com.example.parkingclientapplication.R
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.TextView
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.google.android.gms.maps.model.Marker
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.android.synthetic.main.item_row.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MapParkingFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,LocationSource.OnLocationChangedListener,
    GoogleMap.OnMyLocationClickListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener, ActivityCompat.OnRequestPermissionsResultCallback {



    lateinit var mapView: MapView
    var mapFragment: GoogleMap? = null

    private val NEARCurrentLocation1 = LatLng(37.4258983, -122.084)
    private val NEARCurrentLocation2 = LatLng(37.4219983, -122.184)
    private val NEARCurrentLocation3 = LatLng(37.4210083, -122.043)

    private var mNEARCurrentLocation1: Marker? = null
    private var mNEARCurrentLocation2: Marker? = null
    private var mNEARCurrentLocation3: Marker? = null

    private var permissions: ArrayList<String> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()

    private lateinit var loadFragments: LoadFragments

    private var location: Location? = null

    private var uiSettings: UiSettings? = null

    private var currentLocation: Location? = null

    private var isEnabled: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map_parking, container, false)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        loadFragments = activity as LoadFragments

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        mapView.getMapAsync(this)
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(R.menu.map_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_location ->{
                manageLocation()
                true
            }
            R.id.action_clear ->{
                //markerList.clear()
                for (i in 0 until markerList.size){
                    if (markerList[i].title == "Mi localizacion"){
                        markerList[i].remove()
                    }
                }
                currentLocation = Location("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mapFragment = googleMap!!

        // Add a marker in Sydney, Australia, and move the camera.
        mNEARCurrentLocation1 = mapFragment!!
            .addMarker(MarkerOptions()
                .position(NEARCurrentLocation1)
                .title("Perth")
                .icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )
        mNEARCurrentLocation2 = mapFragment!!
            .addMarker(MarkerOptions()
                .position(NEARCurrentLocation2)
                .title("BRISBANE")
                .icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        mNEARCurrentLocation3 = mapFragment!!
            .addMarker(MarkerOptions()
                .position(NEARCurrentLocation3)
                .title("Sydney")
                .icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

        mNEARCurrentLocation1!!.tag = 0
        mNEARCurrentLocation2!!.tag = 0
        mNEARCurrentLocation3!!.tag = 0

        uiSettings = mapFragment!!.uiSettings

        uiSettings!!.isMyLocationButtonEnabled = true
        uiSettings!!.isZoomControlsEnabled = true
        uiSettings!!.isCompassEnabled = true

        mapFragment!!.setOnMyLocationClickListener(this)
        mapFragment!!.setOnMyLocationButtonClickListener(this)
        mapFragment!!.setOnMarkerClickListener(this)

        mapFragment!!.setInfoWindowAdapter(object:GoogleMap.InfoWindowAdapter {
            private var mContents: View? = null

            override fun getInfoWindow(marker:Marker):View? {
                return null
            }
            override fun getInfoContents(marker:Marker):View {
                mContents = activity!!.layoutInflater.inflate(R.layout.custom_info_layout, null)

                when {
                    marker.title == "Perth" -> {
                        (mContents!!.findViewById(R.id.info_window_parking) as TextView).text = "Aparcamiento Perth"
                        (mContents!!.findViewById(R.id.info_window_direction) as TextView).text = "Avenida de Juan"
                        (mContents!!.findViewById(R.id.info_window_state) as TextView).text = "Estado: Activo 24/7"
                        (mContents!!.findViewById(R.id.info_window_free) as TextView).text = "Plazas: 50/100"
                        (mContents!!.findViewById(R.id.info_window_coste) as TextView).text = "3€/h"
                    }
                    marker.title == "BRISBANE" -> {
                        (mContents!!.findViewById(R.id.info_window_parking) as TextView).text = "Aparcamiento BRISBANE"
                        (mContents!!.findViewById(R.id.info_window_direction) as TextView).text = "Avenida de Pedro"
                        (mContents!!.findViewById(R.id.info_window_state) as TextView).text = "Estado: Activo 24/7"
                        (mContents!!.findViewById(R.id.info_window_free) as TextView).text = "Plazas: 90/100"
                        (mContents!!.findViewById(R.id.info_window_coste) as TextView).text = "3€/h"

                    }
                    marker.title == "Sydney" -> {
                        (mContents!!.findViewById(R.id.info_window_parking) as TextView).text = "Aparcamiento Sydney"
                        (mContents!!.findViewById(R.id.info_window_direction) as TextView).text = "Avenida de Jorge"
                        (mContents!!.findViewById(R.id.info_window_state) as TextView).text = "Estado: Activo 24/7"
                        (mContents!!.findViewById(R.id.info_window_free) as TextView).text = "Plazas: 23/70"
                        (mContents!!.findViewById(R.id.info_window_coste) as TextView).text = "2€/h"

                    }
                }

                return mContents!!
            }
        })
        mapFragment!!.moveCamera(CameraUpdateFactory.newLatLng(NEARCurrentLocation1))
        mapFragment!!.setOnInfoWindowClickListener(this)


    }

    override fun onMyLocationButtonClick(): Boolean {
        getCurrentLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        getAddressFromLocation(location, context!!)
    }

    override fun onRequestPermissionsResult(requestCode:Int,
                                            permissions:Array<String>, grantResults:IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if ((ContextCompat.checkSelfPermission(
                            activity!!,
                            permissions[0]) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                            activity!!,
                            permissions[1]) == PackageManager.PERMISSION_GRANTED))
                    {
                        mapFragment!!.isMyLocationEnabled = true
                    }
                }
                else
                {
                }
                return
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        //getDirection(marker!!.position)
        // Retrieve the data from the marker.
        /*var clickCount:Int? = marker!!.tag as Int

        // Check if a click count was set, then display the click count.
        if (clickCount != null)
        {
            clickCount += 1
            marker.tag = clickCount
            Toast.makeText(context,marker.title + " has been clicked " + clickCount + " times.", Toast.LENGTH_SHORT).show()
        }*/

       return false
    }

    override fun onInfoWindowClick(p0: Marker?) {
        try
        {
            Handler().postDelayed({  loadFragments.loadFragment(3) }, 100)
            return
        }
        catch (e:Exception) {
            Log.e("Exception", "Exception :: " + e.message)
        }
    }

    override fun onLocationChanged(location: Location?) {
        getCurrentLocation()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    //Permite la selecciono y deselección de la localización del dispositivo
    private fun manageLocation(){
        if(!isEnabled) {
            if (checkLocationPermission()) {
                if ((ContextCompat.checkSelfPermission(
                        context!!,
                        permissions[0]
                    )
                            == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                        context!!,
                        permissions[1]
                    )
                            == PackageManager.PERMISSION_GRANTED)
                ) {
                    uiSettings!!.isMyLocationButtonEnabled = true

                    mapFragment!!.isMyLocationEnabled = true
                    isEnabled = true
                }
            }
        }else{
            if (checkLocationPermission()) {
                if ((ContextCompat.checkSelfPermission(
                        context!!,
                        permissions[0]
                    )
                            == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                        context!!,
                        permissions[1]
                    )
                            == PackageManager.PERMISSION_GRANTED)
                ) {
                    uiSettings!!.isMyLocationButtonEnabled = false
                    mapFragment!!.isMyLocationEnabled = false
                    isEnabled = false
                }
            }
        }
    }

    private fun getDirection(marker: LatLng){
        GoogleDirection.withServerKey("AIzaSyBn0JtWo8gDx4MIUwBdGISs7_YimRHqy7A")
            .from(LatLng(location!!.latitude, location!!.longitude))
            .to(LatLng(marker.latitude, marker.longitude))
            .execute(object: DirectionCallback {
                override fun onDirectionSuccess(direction: Direction, rawBody:String) {
                    if (direction.isOK)
                    {
                        val route = direction.routeList[0]
                        val leg = route.legList[0]
                        val directionPositionList = leg.directionPoint
                        val polylineOptions = DirectionConverter.createPolyline(context, directionPositionList, 5, Color.RED)
                        mapFragment!!.addPolyline(polylineOptions)
                    }
                    else
                    {
                        // Do something
                    }
                }
                override fun onDirectionFailure(t:Throwable) {
                    // Do something
                }
            })
    }

    //Obtiene la localización actual del dispositivo y lo asocia a un marcador
    private fun getCurrentLocation() {

        if (checkLocationPermission()) {
            if ((ContextCompat.checkSelfPermission(
                    context!!,
                    permissions[0]
                )
                        == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    context!!,
                    permissions[1]
                )
                        == PackageManager.PERMISSION_GRANTED)
            ) {

                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        currentLocation = location
                        drawMarker(currentLocation!!, "Mi localizacion")
                    }

                    .addOnFailureListener { e -> e.printStackTrace() }
            }
        }
    }

    private fun getAddressFromLocation(location: Location, context: Context){
        val geocoder = Geocoder(context, Locale.getDefault())
        var result:String? = null
        try {
            val list = geocoder.getFromLocation(
                location.latitude, location.longitude, 1)
            if (list != null && list.size > 0) {
                val address = list[0]
                result = address.getAddressLine(0) + ", " + address.locality

            }
        } catch (e: IOException) {

        } finally {
            if (result != null) {
                Toast.makeText(context, "Dirección: $result", Toast.LENGTH_LONG).show()
            } else {}
        }
    }

    private fun checkLocationPermission(): Boolean {
        when {
            ContextCompat.checkSelfPermission(
                activity!!,
                permissions[0]
            ) != PackageManager.PERMISSION_GRANTED -> {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity!!,
                        permissions[0]
                    )
                ) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder(activity!!)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok") { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(
                                activity!!,
                                arrayOf(permissions[0]),
                                1
                            )
                        }
                        .create()
                        .show()


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(permissions[0]),
                        1
                    )
                }
                return false
            }
            ContextCompat.checkSelfPermission(
                activity!!,
                permissions[1]
            ) != PackageManager.PERMISSION_GRANTED -> {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity!!,
                        permissions[1]
                    )
                ) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder(activity!!)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok") { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(
                                activity!!,
                                arrayOf(permissions[1]),
                                1
                            )
                        }
                        .create()
                        .show()


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(permissions[1]),
                        1
                    )
                }
                return false
            }
            else -> return true
        }
    }

    private fun drawMarker(location: Location, title: String) {
        val gps = LatLng(location.latitude, location.longitude)

        var indexMarker: Int = -1

        val marker = MarkerOptions()
            .position(gps)
            .title(title)
        for (i in 0 until markerList.size) {
            if (markerList[i].title == marker!!.title && marker.title == "Mi localizacion") {
                indexMarker = i
                break
            }
        }
        if(indexMarker != -1){
            markerList[indexMarker].remove()
            markerList.removeAt(indexMarker)
            markerList.add(mapFragment!!.addMarker(marker))
        }else{
            markerList.add(mapFragment!!.addMarker(marker))
        }

    }


}
