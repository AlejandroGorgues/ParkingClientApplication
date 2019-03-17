package com.example.parkingclientapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class VehicleListAdapter(private var vehicles : Array<String>, private val context: Context) : RecyclerView.Adapter<ManagerVehViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ManagerVehViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.client_vehicles_card_view, parent, false)

        vh.setOnClickListener(this)
        return ManagerVehViewHolder(vh)
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }

    override fun onBindViewHolder(cvh: ManagerVehViewHolder, pos: Int) {
        val item = vehicles[pos]
        cvh.bindVehicles(item, context)
    }

    override fun onClick(view: View?) {
        if (listener != null)
            listener!!.onClick(view)
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }
}



class ManagerVehViewHolder (viewVeh: View) : RecyclerView.ViewHolder(viewVeh) {

    private var vehicle: TextView = viewVeh.findViewById(R.id.vehicleManager)

    fun bindVehicles(o: String, context: Context) {

        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        vehicle.text = o

    }

}