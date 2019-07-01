package com.example.parkingclientapplication.activities

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.example.parkingclientapplication.GeofenceTransitionIntentService
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Parking
import com.example.parkingclientapplication.model.Reservation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.app_bar_guide_map.*
import kotlin.collections.ArrayList


class GuideMapActivity : AppCompatActivity(), OnMapReadyCallback, LocationSource.OnLocationChangedListener,
    GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback{



    private lateinit var mapView: MapView
    var mapFragment: GoogleMap? = null
    private var permissions: ArrayList<String> = ArrayList()

    private var uiSettings: UiSettings? = null
    private var locationLatLng: LatLng? = null

    private var currentLocation: Location? = null
    private var parkingLocation: Location? = null

    private lateinit var geofencingClient: GeofencingClient

    private var  geoFenceLimits: Circle? = null

    private var geofenceList = ArrayList<Geofence>()

    private lateinit var parking: Parking
    private lateinit var reservation: Reservation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide_map)

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        currentLocation = Location("")
        parkingLocation = Location("")

        setSupportActionBar(GuideMaptoolbar)

        GuideMaptoolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        GuideMaptoolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 1){

                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)
            }else{
                supportFragmentManager.popBackStack()
            }

        }

        parking = intent.getBundleExtra("parkingSelected").getParcelable("parking")!!
        reservation = intent.getBundleExtra("parkingSelected").getParcelable("reservation")!!

        mapView = findViewById(R.id.mapGuideView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        mapView.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.guide_map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_guide ->{
                startGuiding()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mapFragment = googleMap!!
        drawMarker(LatLng(parking.latitude!!.toDouble(), parking.longitude!!.toDouble()), parking.nameParking!!)



        uiSettings = mapFragment!!.uiSettings

        uiSettings!!.isMyLocationButtonEnabled = true
        uiSettings!!.isZoomControlsEnabled = true
        uiSettings!!.isCompassEnabled = true

        mapFragment!!.setOnMyLocationButtonClickListener(this)
        mapFragment!!.setOnMarkerClickListener(this)
    }

    override fun onLocationChanged(location: Location?) {
        getCurrentLocation()
    }

    override fun onMyLocationButtonClick(): Boolean {
        getCurrentLocation()
        return false
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
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
                            this,
                            permissions[0]) == PackageManager.PERMISSION_GRANTED))
                    {
                        mapFragment!!.isMyLocationEnabled = true
                        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                        mFusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                currentLocation = location
                                locationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                                drawMarker(locationLatLng!!, "Mi localizacion")
                            }

                            .addOnFailureListener { e -> e.printStackTrace() }
                    }
                }
                else
                {
                }
                return
            }
        }
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

    /*
    *When user press the top right button, start guiding
     */
    private fun startGuiding(){
        createParkingGeofence(parking.latitude!!, parking.longitude!!)
        getCurrentLocation()

        // Due to the limited use of directions API, I commented the direction call
        //getDirection(LatLng(parking.latitude!!.toDouble(), parking.longitude!!.toDouble()))
    }

    /*
    * Draw the polilyne of the route to the parking
     */
    private fun getDirection(marker: LatLng){
        GoogleDirection.withServerKey(resources.getString(R.string.googleDirection))
            .from(LatLng(locationLatLng!!.latitude, locationLatLng!!.longitude))
            .to(LatLng(marker.latitude, marker.longitude))
            .execute(object: DirectionCallback {
                override fun onDirectionSuccess(direction: Direction, rawBody:String) {
                    if (direction.isOK)
                    {
                        val route = direction.routeList[0]
                        val leg = route.legList[0]
                        val directionPositionList = leg.directionPoint
                        val polylineOptions = DirectionConverter.createPolyline(this@GuideMapActivity, directionPositionList, 5, Color.RED)
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

    //Obtain current location and draw a marker on it
    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            if ((ContextCompat.checkSelfPermission(
                    this,
                    permissions[0]
                )
                        == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    this,
                    permissions[1]
                )
                        == PackageManager.PERMISSION_GRANTED)
            ) {

                    mapFragment!!.isMyLocationEnabled = true
                    val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    mFusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            currentLocation = location
                            locationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                            drawMarker(locationLatLng!!, "Mi localizacion")
                        }

                        .addOnFailureListener { e -> e.printStackTrace() }

                }
            }
    }

    private fun checkLocationPermission(): Boolean {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permissions[0]
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                permissions[1]
            ) != PackageManager.PERMISSION_GRANTED  -> {

                // Should we show an explanation?
                if ((ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permissions[0]
                    )) && (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permissions[0]
                    ))
                ){

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder(this)
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok") { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(
                                this,
                                permissions.toTypedArray(),
                                1
                            )
                        }
                        .create()
                        .show()


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        permissions.toTypedArray(),
                        1
                    )
                }
                return false
            }
            else -> return true
        }
    }

    //Create the geofence over the parking marker
    private fun createParkingGeofence(latitude: Float, longitude: Float) {


        parkingLocation!!.latitude = latitude.toDouble()
        parkingLocation!!.longitude = longitude.toDouble()
        addGeofenceList(parkingLocation!!)
        addGeofenceClient()
        drawGeofence(parkingLocation!!)
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
        builder.addGeofences(geofenceList)
        return builder.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val bundle = Bundle()
        val intent = Intent(this, GeofenceTransitionIntentService::class.java)
        bundle.putParcelable("reservation", reservation)
        intent.putExtra("reservationSelected", bundle)
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun addGeofenceClient(){
        if ((ContextCompat.checkSelfPermission(
                this,
                permissions[0]
            )
                    == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                permissions[1]
            )
                    == PackageManager.PERMISSION_GRANTED)
        ) {

            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added
                    // ...

                    //Toast.makeText(applicationContext,"geofenceadded", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    // Failed to add geofences
                    // ...
                }
            }
        }
    }

    private fun addGeofenceList(location:Location){
        geofenceList.add(Geofence.Builder()
            .setRequestId(parking.id)

            .setCircularRegion(
                location.latitude,
                location.longitude,
                1609F
            )

            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(1000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build())

    }

    private fun drawGeofence(location:Location) {

        if (geoFenceLimits != null)
            geoFenceLimits!!.remove()

        val latlong = LatLng(location.latitude, location.longitude)

        val circleOptions = CircleOptions()
            .center(latlong)
            .strokeColor(Color.argb(50, 18, 54, 82))
            .fillColor(Color.argb(100, 41, 79, 109))
            .radius(1609.0)
        geoFenceLimits = mapFragment!!.addCircle(circleOptions)
    }

    private fun drawMarker(location: LatLng, title: String) {

        val marker = MarkerOptions()
            .position(location)
            .title(title)

        mapFragment!!.addMarker(marker)
    }
}
