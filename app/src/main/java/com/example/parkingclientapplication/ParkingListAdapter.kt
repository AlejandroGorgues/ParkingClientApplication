package com.example.parkingclientapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ParkingListAdapter(private var parkings : Array<String>, private val context: Context) : RecyclerView.Adapter<ManagerParkingViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ManagerParkingViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.client_parkings_card_view, parent, false)

        vh.setOnClickListener(this)
        return ManagerParkingViewHolder(vh)
    }

    override fun getItemCount(): Int {
        return parkings.size
    }

    override fun onBindViewHolder(cvh: ManagerParkingViewHolder, pos: Int) {
        val item = parkings[pos]
        cvh.bindParkings(item, context)
    }

    override fun onClick(view: View?) {
        if (listener != null)
            listener!!.onClick(view)
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }
}



class ManagerParkingViewHolder (viewParking: View) : RecyclerView.ViewHolder(viewParking) {

    private var parking: TextView = viewParking.findViewById(R.id.parkingManager)

    fun bindParkings(o: String, context: Context) {

        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        parking.text = o

    }

}