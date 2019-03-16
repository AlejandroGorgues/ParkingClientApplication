package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.example.parkingclientapplication.R


class VehicleAddFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var  vehSpinner: Spinner
    private lateinit var continueB: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_add, container, false)

        vehSpinner = view.findViewById(R.id.vehicles_spinner)
        vehSpinner.onItemSelectedListener = this

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val vehAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.vehicles_array))
        // Set layout to use when the list of choices appear
        vehAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        vehSpinner.adapter = vehAdapter

        continueB = view.findViewById(R.id.buttonContinue1)
        continueB.setOnClickListener {

        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if(position == 0)
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
