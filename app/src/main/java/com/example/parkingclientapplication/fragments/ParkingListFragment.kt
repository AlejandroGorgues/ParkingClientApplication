package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parkingclientapplication.ParkingListAdapter

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments
import android.os.StrictMode
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import android.support.annotation.NonNull
import com.mapbox.mapboxsdk.maps.Style.OnStyleLoaded
import com.mapbox.mapboxsdk.maps.Style.MAPBOX_STREETS
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style


class ParkingListFragment : Fragment() {

    private lateinit var parkAdapter: RecyclerView.Adapter<*>
    //private lateinit var root: View
    private lateinit var parkRecyclerView: RecyclerView
    private val parkings: Array<String> = arrayOf("Parking1", "Parking2", "Parking3", "Parking4", "Parking5", "Parking6")

    private lateinit var loadFragment: LoadFragments

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(context!!, "pk.eyJ1IjoiYWxlZ29ydmFsIiwiYSI6ImNqdDVzZTFjYTA4NnY0NXA5cmloeXQwaDAifQ.Zb-6UMxKqC3E-OaVPC4Kiw")

        val view = inflater.inflate(R.layout.fragment_parking_list, container, false)


        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
        }
        loadFragment = activity as LoadFragments
        parkRecyclerView = view!!.findViewById(R.id.manager_options_recycler_view)
        parkAdapter = ParkingListAdapter(parkings, context!!)
        (parkAdapter as ParkingListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = parkRecyclerView.getChildAdapterPosition(v)



        })



        inicializarReciclerView()
        // Inflate the layout for this fragment
        return view
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

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun inicializarReciclerView(){
        parkRecyclerView.adapter = parkAdapter
        parkRecyclerView.layoutManager = LinearLayoutManager(activity)
        parkRecyclerView.itemAnimator = DefaultItemAnimator()
    }


}
