package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.VehicleListAdapter
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.text.Layout
import android.widget.ImageButton
import com.example.parkingclientapplication.VehicleSelectionAdapter
import com.example.parkingclientapplication.interfaces.LoadFragments


class VehicleSelectionFragment : Fragment() {

    private lateinit var vehAdapter: RecyclerView.Adapter<*>
    private lateinit var vehRecyclerView: RecyclerView
    private val vehicles: Array<String> = arrayOf("Vehiculo1", "Vehiculo2", "Vehiculo3", "Vehiculo4", "Vehiculo5", "Vehiculo6")

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_selection, container, false)


        loadFragment = activity as LoadFragments
        vehRecyclerView = view!!.findViewById(R.id.manager_selection_recycler_view)
        vehAdapter = VehicleSelectionAdapter(vehicles, context!!)
        (vehAdapter as VehicleSelectionAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = vehRecyclerView.getChildAdapterPosition(v)
            loadFragment.loadFragment(1)


        })

        inicializarReciclerView()
        // Inflate the layout for this fragment
        return view
    }

    private fun inicializarReciclerView(){
        vehRecyclerView.adapter = vehAdapter
        vehRecyclerView.layoutManager = LinearLayoutManager(activity)
        vehRecyclerView.itemAnimator = DefaultItemAnimator()
    }


}
