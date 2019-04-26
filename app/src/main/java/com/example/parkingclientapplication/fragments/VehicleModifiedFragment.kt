package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Vehicle

class VehicleModifiedFragment : Fragment() {

    private lateinit var edTipoVehiculo: EditText
    private lateinit var edMarcaVehiculo: EditText
    private lateinit var edModeloVehiculo: EditText
    private lateinit var edMatriculaVehiculo: EditText

    private lateinit var vehiculo: Vehicle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_modified, container, false)

        edTipoVehiculo = view.findViewById(R.id.edTipoVehiculo)
        edMarcaVehiculo = view.findViewById(R.id.edMarcaVehiculo)
        edModeloVehiculo = view.findViewById(R.id.edModeloVehiculo)
        edMatriculaVehiculo = view.findViewById(R.id.edMatriculaVehiculo)

        vehiculo = arguments!!.getParcelable("vehiculo")!!

        edTipoVehiculo.setText(vehiculo.type)
        edMarcaVehiculo.setText(vehiculo.brand)
        edModeloVehiculo.setText(vehiculo.model)
        edMatriculaVehiculo.setText(vehiculo.licensePlate)
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(R.menu.client_map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}
