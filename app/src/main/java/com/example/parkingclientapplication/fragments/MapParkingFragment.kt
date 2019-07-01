package com.example.parkingclientapplication.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import com.example.parkingclientapplication.R
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.TextView
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Parking
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.net.MalformedURLException
import kotlin.collections.ArrayList


class MapParkingFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,LocationSource.OnLocationChangedListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener, ActivityCompat.OnRequestPermissionsResultCallback {



    private lateinit var mapView: MapView

    private var mapFragment: GoogleMap? = null

    private var permissions: ArrayList<String> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()

    private lateinit var loadFragments: LoadFragments

    private var locationLatLng: LatLng? = null

    private var uiSettings: UiSettings? = null

    private var currentLocation: Location? = null

    private var isEnabled: Boolean = false
    private var maxOccupation = 0
    private var occupation = 0

    private var mClient: MobileServiceClient? = null

    private var parkingTable: MobileServiceTable<Parking>? = null

    private var parkings = ArrayList<Parking>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map_parking, container, false)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        loadFragments = activity as LoadFragments

        currentLocation = Location("")

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
            R.id.action_location ->{
                manageLocation()
                true
            }
            R.id.action_clear ->{
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


        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()

            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }


            //Obtain all parkings and draw them on the map
            parkingTable = mClient!!.getTable(Parking::class.java)
            doAsync {

                val resultQueryParking = parkingTable!!.execute().get()
                for (parking in resultQueryParking) {
                    parkings.add(parking)


                    context!!.runOnUiThread {
                        locationLatLng = LatLng(parking.latitude!!.toDouble(), parking.longitude!!.toDouble())
                        drawMarker(locationLatLng!!, parking.nameParking!!, parking.price!!)
                    }
                }
            }

            //For each parking create an infoWindow to show information about the parking when the user clicks on it
            mapFragment!!.setInfoWindowAdapter(object:GoogleMap.InfoWindowAdapter {
                private var mContents: View? = null

                override fun getInfoWindow(marker:Marker):View? {
                    return null
                }
                override fun getInfoContents(marker:Marker):View {
                    mContents = activity!!.layoutInflater.inflate(R.layout.custom_info_layout, null)


                    val selectedParking: Parking = parkings.single { parking -> parking.nameParking == marker.title}


                    val estado = if (selectedParking.stateParking!!){
                        "Abierto"
                    }else{
                        "Cerrado"
                    }

                    val estadoParking = "Estado: $estado"
                    val ocupacionParking = "Ocupación: " + selectedParking.occupation.toString() + "/" + selectedParking.maxOccupation.toString()
                    val precioParking = "Precio: " + selectedParking.price.toString() +"€/h"

                    (mContents!!.findViewById(R.id.info_window_parking) as TextView).text = selectedParking.nameParking
                    (mContents!!.findViewById(R.id.info_window_direction) as TextView).text = selectedParking.direction
                    (mContents!!.findViewById(R.id.info_window_state) as TextView).text = estadoParking
                    (mContents!!.findViewById(R.id.info_window_free) as TextView).text = ocupacionParking
                    (mContents!!.findViewById(R.id.info_window_coste) as TextView).text = precioParking
                    maxOccupation = 0
                    occupation = 0

                    return mContents!!
                }
            })

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }

        uiSettings = mapFragment!!.uiSettings

        uiSettings!!.isMyLocationButtonEnabled = true
        uiSettings!!.isZoomControlsEnabled = true
        uiSettings!!.isCompassEnabled = true

        mapFragment!!.setOnMyLocationButtonClickListener(this)
        mapFragment!!.setOnMarkerClickListener(this)
        mapFragment!!.setOnInfoWindowClickListener(this)


    }

    override fun onMyLocationButtonClick(): Boolean {
        getCurrentLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
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

       return false
    }

    override fun onInfoWindowClick(marker: Marker?) {
        try
        {
            val bundle = Bundle()
            for (parkingAux in parkings){
                if(parkingAux.nameParking == marker!!.title){
                    bundle.putParcelable("parking", parkingAux)
                    Handler().postDelayed({  loadFragments.loadFragment(3, bundle)}, 100)
                    break
                }
            }
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

    //Activate or desactivate current location on the map
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



    //Obtain current location of the device and draw a marker on it
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
                        locationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                        drawMarker(locationLatLng!!, "Mi localizacion", 0F)
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

    private fun drawMarker(location: LatLng, title: String, price: Float) {


        // Based on the price of the parking, select the color of the marker associated
        val color: BitmapDescriptor = when {
            price > 4  ->
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)
            price > 2.5F && price <= 4  ->
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            price == 2.5F ->
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            price < 2.5F  && price > 0F ->
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            else ->
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }

        var myIndexMarker: Int = -1

        val marker = MarkerOptions()
            .position(location)
            .title(title)
            .icon(color)

        //Si el marker de mi localización se encuentra en la lista, se obtiene su índice
        for (i in 0 until markerList.size) {
            if (markerList[i].title == marker!!.title && marker.title == "Mi localizacion") {
                myIndexMarker = i
                break
            }
        }

        //Si el índice es diferente de -1, se elimina el marker de la lista al estar repetido
        //Si no, se añade a la lista
        if(myIndexMarker != -1){
            markerList[myIndexMarker].remove()
            markerList.removeAt(myIndexMarker)
            markerList.add(mapFragment!!.addMarker(marker))
        }else{
            markerList.add(mapFragment!!.addMarker(marker))
        }

    }
}
