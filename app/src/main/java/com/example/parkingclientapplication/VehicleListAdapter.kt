package com.example.parkingclientapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class VehicleListAdapter(private var opciones : Array<String>, private val context: Context) : RecyclerView.Adapter<ManagerOptViewHolder>(), View.OnClickListener {

    private var listener: View.OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ManagerOptViewHolder {
        val vh = LayoutInflater.from(parent.context).inflate(R.layout.client_vehicles_card_view, parent, false)

        vh.setOnClickListener(this)
        return ManagerOptViewHolder(vh)
    }

    override fun getItemCount(): Int {
        return opciones.size
    }

    override fun onBindViewHolder(cvh: ManagerOptViewHolder, pos: Int) {
        val item = opciones[pos]
        cvh.bindContactos(item, context)
    }

    override fun onClick(view: View?) {
        if (listener != null)
            listener!!.onClick(view)
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }
}



class ManagerOptViewHolder (viewOpt: View) : RecyclerView.ViewHolder(viewOpt) {

    private var option: TextView = viewOpt.findViewById(R.id.vehicleManager)

    fun bindContactos(o: String, context: Context) {

        /*val androidColors =   context.resources.getIntArray(R.array.agendaColors)
        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]

        val drawable = circuloView.background as GradientDrawable
        drawable.setColor(randomAndroidColor)

        circuloView.text = c.nombre!![0].toString().toUpperCase()*/
        option.text = o

    }

}