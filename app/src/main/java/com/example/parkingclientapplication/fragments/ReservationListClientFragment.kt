package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.ReservationListAdapter
import com.example.parkingclientapplication.VehicleListAdapter
import com.example.parkingclientapplication.interfaces.LoadFragments

class ReservationListClientFragment : Fragment() {

    private lateinit var reservAdapter: RecyclerView.Adapter<*>
    private lateinit var reservRecyclerView: RecyclerView
    private val reservations: Array<String> = arrayOf("Reserva1", "Reserva2", "Reserva3", "Reserva4", "Reserva5", "Reserva6")

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_reservation_list_client, container, false)
        loadFragment = activity as LoadFragments
        reservRecyclerView = view!!.findViewById(R.id.manager_selection_recycler_view)
        reservAdapter = ReservationListAdapter(reservations, context!!)
        (reservAdapter as ReservationListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = reservRecyclerView.getChildAdapterPosition(v)
            loadFragment.loadFragment(5)


        })
        inicializarReciclerView()
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
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
        reservRecyclerView.adapter = reservAdapter
        reservRecyclerView.layoutManager = LinearLayoutManager(activity)
        reservRecyclerView.itemAnimator = DefaultItemAnimator()
    }

}
