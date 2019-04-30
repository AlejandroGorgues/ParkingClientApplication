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
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.SingletonHolder
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Driver
import com.example.parkingclientapplication.model.Parking
import com.example.parkingclientapplication.model.ParkingLot
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.android.synthetic.main.item_row.*
import kotlinx.android.synthetic.main.nav_header_client_map.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.net.MalformedURLException
import java.util.*
import kotlin.collections.ArrayList


class MapParkingFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,LocationSource.OnLocationChangedListener,
    GoogleMap.OnMyLocationClickListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener, ActivityCompat.OnRequestPermissionsResultCallback {



    lateinit var mapView: MapView

    //private lateinit var txtUsername: TextView
    //private lateinit var txtEmail: TextView
    var mapFragment: GoogleMap? = null

    private var permissions: ArrayList<String> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()

    private lateinit var loadFragments: LoadFragments

    private var locationLatLng: LatLng? = null

    private var uiSettings: UiSettings? = null

    private var currentLocation: Location? = null
    private var randomLocation: Location? = null

    private var isEnabled: Boolean = false
    private var maxOccupation = 0
    private var occupation = 0

    private var mClient: MobileServiceClient? = null

    private var parkingTable: MobileServiceTable<Parking>? = null
    private var parkingLotTable: MobileServiceTable<ParkingLot>? = null

    private var parkings = ArrayList<Parking>()

    private lateinit var viewAux: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map_parking, container, false)
        viewAux = view
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        loadFragments = activity as LoadFragments

        currentLocation = Location("")
        randomLocation = Location("")



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
                //markerList.clear()
                for (i in 0 until markerList.size){
                    if (markerList[i].title == "Mi localizacion"){
                        markerList[i].remove()
                    }
                }
                currentLocation = Location("")
                randomLocation = Location("")
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

            mapFragment!!.setInfoWindowAdapter(object:GoogleMap.InfoWindowAdapter {
                private var mContents: View? = null

                override fun getInfoWindow(marker:Marker):View? {
                    return null
                }
                override fun getInfoContents(marker:Marker):View {
                    mContents = activity!!.layoutInflater.inflate(R.layout.custom_info_layout, null)


                    val selectedParking: Parking = parkings.single { parking -> parking.nameParking == marker.title}


                    val estado = if (selectedParking.stateParking!!){
                        "abierto"
                    }else{
                        "Cerrado"
                    }

                    (mContents!!.findViewById(R.id.info_window_parking) as TextView).text = selectedParking.nameParking
                    (mContents!!.findViewById(R.id.info_window_direction) as TextView).text = selectedParking.direction
                    (mContents!!.findViewById(R.id.info_window_state) as TextView).text = estado
                    (mContents!!.findViewById(R.id.info_window_free) as TextView).text = selectedParking.occupation.toString() + "/" + selectedParking.maxOccupation.toString()
                    (mContents!!.findViewById(R.id.info_window_coste) as TextView).text = selectedParking.price.toString() +"€/h"
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


        //mNEARCurrentLocation1!!.tag = 0

        uiSettings = mapFragment!!.uiSettings

        uiSettings!!.isMyLocationButtonEnabled = true
        uiSettings!!.isZoomControlsEnabled = true
        uiSettings!!.isCompassEnabled = true

        mapFragment!!.setOnMyLocationClickListener(this)
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
            .from(LatLng(locationLatLng!!.latitude, locationLatLng!!.longitude))
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
                        locationLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                        drawMarker(locationLatLng!!, "Mi localizacion", 0F)
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

    private fun drawMarker(location: LatLng, title: String, price: Float) {


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
