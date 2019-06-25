package com.example.parkingclientapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.parkingclientapplication.interfaces.UpdateVehicleList
import com.example.parkingclientapplication.model.Vehicle

class VehicleListAdapter(private var vehicles : ArrayList<Vehicle>, private var vehicleInterface: UpdateVehicleList) : RecyclerView.Adapter<ManagerVehViewHolder>(), View.OnClickListener {

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
        cvh.bindVehicles(item, vehicleInterface)
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

    private var licensePlate: TextView = viewVeh.findViewById(R.id.vehicleLicensePlate)
    private var brand: TextView = viewVeh.findViewById(R.id.vehicleBrand)
    private var model: TextView = viewVeh.findViewById(R.id.vehicleModel)
    private var delButton: ImageButton = viewVeh.findViewById(R.id.deleteCarButton)


    fun bindVehicles(vehicle: Vehicle, updateList: UpdateVehicleList) {


        delButton.setOnClickListener {

            updateList.updateList(vehicle)
        }
        licensePlate.text = "Matrícula: " + vehicle.licensePlate
        brand.text = "Marca: " + vehicle.brand
        model.text = "Modelo: " + vehicle.model

    }

}