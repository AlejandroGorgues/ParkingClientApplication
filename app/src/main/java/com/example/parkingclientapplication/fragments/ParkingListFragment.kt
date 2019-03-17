package com.example.parkingclientapplication.fragments

import android.content.Intent
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
import com.example.parkingclientapplication.activities.MapDirectionActivity
import com.example.parkingclientapplication.interfaces.LoadFragments



class ParkingListFragment : Fragment(){

    private lateinit var parkAdapter: RecyclerView.Adapter<*>
    private lateinit var parkRecyclerView: RecyclerView
    private val parkings: Array<String> = arrayOf("Parking1", "Parking2", "Parking3", "Parking4", "Parking5", "Parking6")

    private lateinit var loadFragment: LoadFragments


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_parking_list, container, false)

        loadFragment = activity as LoadFragments
        parkRecyclerView = view!!.findViewById(R.id.manager_options_recycler_view)
        parkAdapter = ParkingListAdapter(parkings, context!!)
        (parkAdapter as ParkingListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = parkRecyclerView.getChildAdapterPosition(v)
            val intent = Intent(activity, MapDirectionActivity::class.java)
            startActivity(intent)
        })



        inicializarReciclerView()
        // Inflate the layout for this fragment
        return view
    }

    private fun inicializarReciclerView(){
        parkRecyclerView.adapter = parkAdapter
        parkRecyclerView.layoutManager = LinearLayoutManager(activity)
        parkRecyclerView.itemAnimator = DefaultItemAnimator()
    }



}
