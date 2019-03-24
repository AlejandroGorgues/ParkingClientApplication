package com.example.parkingclientapplication.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
import android.widget.TextView
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.google.android.gms.maps.model.Marker
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory


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

    private lateinit var loadFragments: LoadFragments

    private var location: Location? = null


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


        // Inflate the layout for this fragment
        return view
    }



    override fun onMapReady(googleMap: GoogleMap?) {
        mapFragment = googleMap!!
        if (checkLocationPermission()) {
            if ((ContextCompat.checkSelfPermission(
                    activity!!,
                    permissions[0])
                == PackageManager.PERMISSION_GRANTED)&& (ContextCompat.checkSelfPermission(
                    activity!!,
                    permissions[1])
                        == PackageManager.PERMISSION_GRANTED)) {

                //Request location updates:
                googleMap.isMyLocationEnabled = true
            }
        }
        // Add a marker in Sydney, Australia, and move the camera.
        mNEARCurrentLocation1 = mapFragment!!.addMarker(MarkerOptions().position(NEARCurrentLocation1).title("Perth"))
        mNEARCurrentLocation2 = mapFragment!!.addMarker(MarkerOptions().position(NEARCurrentLocation2).title("BRISBANE"))
        mNEARCurrentLocation3 = mapFragment!!.addMarker(MarkerOptions().position(NEARCurrentLocation3).title("Sydney"))

        mNEARCurrentLocation1!!.tag = 0
        mNEARCurrentLocation2!!.tag = 0
        mNEARCurrentLocation3!!.tag = 0






        mapFragment!!.setInfoWindowAdapter(object:GoogleMap.InfoWindowAdapter {
            private var mContents: View? = null

            override fun getInfoWindow(marker:Marker):View? {
                return null
            }
            override fun getInfoContents(marker:Marker):View {
                mContents = activity!!.layoutInflater.inflate(R.layout.custom_info_layout, null)
                (mContents!!.findViewById(R.id.info_window_nombre) as TextView).text = "Lina Cortés"
                (mContents!!.findViewById(R.id.info_window_placas) as TextView).text = "Placas: SRX32"
                (mContents!!.findViewById(R.id.info_window_estado) as TextView).text = "Estado: Activo"
                return mContents!!
            }
        })
        mapFragment!!.moveCamera(CameraUpdateFactory.newLatLng(NEARCurrentLocation1))

        mapFragment!!.setOnMyLocationButtonClickListener(this)
        mapFragment!!.setOnMyLocationClickListener(this)
        mapFragment!!.setOnMarkerClickListener(this)
        mapFragment!!.setOnInfoWindowClickListener(this)


    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        getCurrentLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(context, "Current location:\n$location", Toast.LENGTH_LONG).show()
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
        var clickCount:Int? = marker!!.tag as Int

        // Check if a click count was set, then display the click count.
        if (clickCount != null)
        {
            clickCount += 1
            marker.tag = clickCount
            Toast.makeText(context,marker.title + " has been clicked " + clickCount + " times.", Toast.LENGTH_SHORT).show()
        }

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
        Log.e("aqui", location!!.latitude.toString())
        val mp1 = MarkerOptions()
        mp1.position(
            LatLng(
                location.latitude,
                location.longitude
            )
        )

        mp1.icon(
            BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
        )
        mapFragment!!.addMarker(mp1)
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

    private fun getCurrentLocation() {


        if (checkLocationPermission()) {
            if ((ContextCompat.checkSelfPermission(
                    activity!!,
                    permissions[0]
                )
                        == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                    activity!!,
                    permissions[1]
                )
                        == PackageManager.PERMISSION_GRANTED)
            ) {

                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // GPS location can be null if GPS is switched off
                        val currentLat = location.latitude
                        val currentLong = location.longitude
                        Log.e("aqui", "$currentLat $currentLong")
                        this.location = location
                    }

                    .addOnFailureListener { e -> e.printStackTrace() }
            }
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


}
