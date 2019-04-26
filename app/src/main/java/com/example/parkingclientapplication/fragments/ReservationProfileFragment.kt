package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Reservation
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ReservationProfileFragment : Fragment() {
    private lateinit var edUserReserv: EditText
    private lateinit var edParkingReserv: EditText
    private lateinit var edTarjetaReserv: EditText
    private lateinit var edMatriculaReserv: EditText
    private lateinit var edTiempoReserv: EditText
    private lateinit var edFechaReserv: EditText
    private lateinit var edEstadoReserv: EditText
    private lateinit var edPrecioReserv: EditText

    private lateinit var reservation: Reservation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_reservation_profile, container, false)

        edUserReserv = view.findViewById(R.id.edUserReserv)
        edParkingReserv = view.findViewById(R.id.edParkingReserv)
        edTarjetaReserv = view.findViewById(R.id.edTarjetaReserv)
        edMatriculaReserv = view.findViewById(R.id.edMatriculaReserv)
        edTiempoReserv = view.findViewById(R.id.edTiempoReserv)
        edFechaReserv = view.findViewById(R.id.edFechaReserv)
        edEstadoReserv = view.findViewById(R.id.edEstadoReserv)
        edPrecioReserv = view.findViewById(R.id.edPrecioReserv)

        reservation = arguments!!.getParcelable("reservation")!!

        edUserReserv.setText("Usuario")
        edParkingReserv.setText("Parking reserva")
        edTarjetaReserv.setText("Tarjeta usuario")
        edMatriculaReserv.setText(reservation.licensePlate)
        edTiempoReserv.setText(reservation.timeActive)
        edFechaReserv.setText(trimDate(reservation.dateReservation.toString()))
        edEstadoReserv.setText(reservation.state)
        edPrecioReserv.setText(reservation.expensesActive.toString())


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

    private fun trimDate(dateString:String): String{
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
        var dateReservation: Date? = null
        try {
            dateReservation = df.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return DateFormat.getInstance().format(dateReservation)
    }


}
