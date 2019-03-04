package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner

import com.example.parkingclientapplication.R
import android.widget.ArrayAdapter
import android.widget.Toast


class VehicleAdd1TypeFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var  vehSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_add1_type, container, false)

        vehSpinner = view.findViewById(R.id.vehicles_spinner)
        vehSpinner.onItemSelectedListener = this

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val vehAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.vehicles_array))
        // Set layout to use when the list of choices appear
        vehAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vehAdapter.add("This is Hint")
        // Set Adapter to Spinner
        vehSpinner.adapter = vehAdapter


        // Inflate the layout for this fragment
        return view
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if(vehSpinner.selectedItem == "This is Hint Text")
        {

            //Do nothing.
        }
        else{

            Toast.makeText(context, vehSpinner.selectedItem.toString(), Toast.LENGTH_LONG).show()

        }
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }


}
