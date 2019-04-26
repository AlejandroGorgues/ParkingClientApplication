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
import com.example.parkingclientapplication.model.Vehicle

class VehicleListAdapter(private var vehicles : ArrayList<Vehicle>, private val context: Context) : RecyclerView.Adapter<ManagerVehViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null
    private var loadFragments: LoadFragments = context as LoadFragments


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



class ManagerVehViewHolder (viewVeh: View ) : RecyclerView.ViewHolder(viewVeh) {

    private var vehicle: TextView = viewVeh.findViewById(R.id.vehicleManager)
    private var modButton: ImageButton = viewVeh.findViewById(R.id.modifiedCarButton)


    fun bindVehicles(o: Vehicle, context: Context, loadFragments: LoadFragments) {

        val bundle = Bundle()
        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        modButton.setOnClickListener {
         loadFragments.loadFragment(2, bundle)
        }
        vehicle.text = o.licensePlate

    }

}