package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.TextView

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Reservation

class ReservationProfileFragment : Fragment() {
    private lateinit var txtTime: TextView
    private lateinit var txtPrice: TextView
    private lateinit var txtLicensePlate: TextView
    private lateinit var txtModel: TextView
    private lateinit var txtBrand: TextView

    private lateinit var reservation: Reservation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_reservation_profile, container, false)

        txtTime = view.findViewById(R.id.txtTime)
        txtPrice = view.findViewById(R.id.txtPrice)
        txtLicensePlate = view.findViewById(R.id.txtLicensePlate)
        txtModel = view.findViewById(R.id.txtModel)
        txtBrand = view.findViewById(R.id.txtBrand)

        reservation = arguments!!.getParcelable("reservation")!!

        txtTime.text = reservation.dateReservation.toString()
        txtPrice.text = reservation.expenses.toString()
        txtLicensePlate.text  = reservation.licensePlate
        txtModel.text = reservation.model
        txtBrand.text = reservation.brand


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


}
