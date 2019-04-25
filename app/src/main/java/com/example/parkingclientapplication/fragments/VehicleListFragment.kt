package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.VehicleListAdapter
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.text.Layout
import android.view.*
import android.widget.ImageButton
import com.example.parkingclientapplication.interfaces.LoadFragments


class VehicleListFragment : Fragment() {


    private lateinit var vehAdapter: RecyclerView.Adapter<*>
    private lateinit var modImageButton: ImageButton
    private lateinit var vehRecyclerView: RecyclerView
    private val vehicles: Array<String> = arrayOf("Vehiculo1", "Vehiculo2", "Vehiculo3", "Vehiculo4", "Vehiculo5", "Vehiculo6")

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_list, container, false)


        loadFragment = activity as LoadFragments
        vehRecyclerView = view!!.findViewById(R.id.client_vehicle_recycler_view)
        vehAdapter = VehicleListAdapter(vehicles, context!!)
        (vehAdapter as VehicleListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = vehRecyclerView.getChildAdapterPosition(v)
            val bundle = Bundle()
            //loadFragment.loadFragment(5, bundle)


        })

        val fab = view.findViewById(R.id.newVehicle) as FloatingActionButton
        fab.setOnClickListener {
            // Click action
            val bundle = Bundle()
            loadFragment.loadFragment(4, bundle)
        }
        inicializarReciclerView()
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(R.menu.client_map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun inicializarReciclerView(){
        vehRecyclerView.adapter = vehAdapter
        vehRecyclerView.layoutManager = LinearLayoutManager(activity)
        vehRecyclerView.itemAnimator = DefaultItemAnimator()
    }




}
