package com.example.parkingclientapplication

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Reservation

class ReservationListAdapter(private var reservations : ArrayList<Reservation>, private val context: Context) : RecyclerView.Adapter<ReservationViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null
    private var loadFragments: LoadFragments = context as LoadFragments


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
        cvh.bindReservation(item, context, loadFragments)
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

    private var reservation: TextView = viewRes.findViewById(R.id.reservation)
    private var infoButton: ImageButton = viewRes.findViewById(R.id.infoReservation)


    fun bindReservation(o: Reservation, context: Context, loadFragments: LoadFragments) {


        val bundle = Bundle()
        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        infoButton.setOnClickListener {
            loadFragments.loadFragment(2, bundle)
        }
        reservation.text = o.brand

    }

}