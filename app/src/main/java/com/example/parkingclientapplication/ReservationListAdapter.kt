package com.example.parkingclientapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.parkingclientapplication.model.Reservation
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReservationListAdapter(private var reservations : ArrayList<Reservation>) : RecyclerView.Adapter<ReservationViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ReservationViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.client_reservation_card_view, parent, false)

        vh.setOnClickListener(this)
        return ReservationViewHolder(vh)
    }

    override fun getItemCount(): Int {
        return reservations.size
    }

    override fun onBindViewHolder(cvh: ReservationViewHolder, pos: Int) {
        val item = reservations[pos]
        cvh.bindReservation(item)
    }

    override fun onClick(view: View?) {
        if (listener != null)
            listener!!.onClick(view)
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }
}



class ReservationViewHolder (viewRes: View ) : RecyclerView.ViewHolder(viewRes) {

    private var parking: TextView = viewRes.findViewById(R.id.reservation_parking)
    private var date: TextView = viewRes.findViewById(R.id.reservation_date)
    private var vehicle: TextView = viewRes.findViewById(R.id.reservation_vehicle)



    fun bindReservation(reservation: Reservation) {
        parking.text = "Nombre del parking: " + reservation.nameParking
        date.text = "Fecha de reserva: " + trimDate(reservation.dateReservation!!)
        vehicle.text = "Matrícula del vehículo: " + reservation.licensePlate

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