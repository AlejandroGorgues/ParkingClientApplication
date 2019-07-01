package com.example.parkingclientapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.parkingclientapplication.model.Vehicle

class VehicleSelectionAdapter(private var vehicles : ArrayList<Vehicle>) : RecyclerView.Adapter<ManagerVehListViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null


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
        cvh.bindVehicles(item)
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

    private var licensePlate: TextView = viewVeh.findViewById(R.id.vehicleLicensePlateS)
    private var brand: TextView = viewVeh.findViewById(R.id.vehicleBrandS)
    private var model: TextView = viewVeh.findViewById(R.id.vehicleModelS)


    fun bindVehicles(vehicle: Vehicle) {
        val licensePlateText = "Matrícula: " + vehicle.licensePlate
        val brandText = "Marca: " + vehicle.brand
        val modelText = "Modelo: " + vehicle.model

        licensePlate.text = licensePlateText
        brand.text = brandText
        model.text = modelText

    }

}