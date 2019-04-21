package com.example.parkingclientapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.parkingclientapplication.interfaces.LoadFragments

class VehicleSelectionAdapter(private var vehicles : Array<String>, private val context: Context) : RecyclerView.Adapter<ManagerVehListViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null
    private var loadFragments: LoadFragments = context as LoadFragments


    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ManagerVehListViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.client_vehicles_selection_card_view, parent, false)

        vh.setOnClickListener(this)
        return ManagerVehListViewHolder(vh)
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }

    override fun onBindViewHolder(cvh: ManagerVehListViewHolder, pos: Int) {
        val item = vehicles[pos]
        cvh.bindVehicles(item, context, loadFragments)
    }

    override fun onClick(view: View?) {
        if (listener != null)
            listener!!.onClick(view)
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }
}



class ManagerVehListViewHolder (viewVeh: View ) : RecyclerView.ViewHolder(viewVeh) {

    private var vehicle: TextView = viewVeh.findViewById(R.id.vehicleManager)


    fun bindVehicles(o: String, context: Context, loadFragments: LoadFragments) {


        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        vehicle.text = o

    }

}