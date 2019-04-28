package com.example.parkingclientapplication

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.interfaces.UpdateVehicleList
import com.example.parkingclientapplication.model.Vehicle
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable

class VehicleListAdapter(private var vehicles : ArrayList<Vehicle>, private val context: Context, private var vehicleTable: MobileServiceTable<Vehicle>, private var vehicleInterface: UpdateVehicleList) : RecyclerView.Adapter<ManagerVehViewHolder>(), View.OnClickListener {

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
        cvh.bindVehicles(item, context, vehicleTable, vehicleInterface)
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
    private var delButton: ImageButton = viewVeh.findViewById(R.id.deleteCarButton)


    fun bindVehicles(o: Vehicle, context: Context, vehicleTable: MobileServiceTable<Vehicle>, updateList: UpdateVehicleList) {

        val bundle = Bundle()
        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        delButton.setOnClickListener {

            updateList.updateList(o)


        }
        vehicle.text = o.licensePlate

    }

}