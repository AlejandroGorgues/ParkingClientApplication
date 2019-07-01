package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Driver
import com.example.parkingclientapplication.model.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class VehicleAddFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var  vehSpinner: Spinner
    private lateinit var continueB: Button
    private lateinit var vehicle:Vehicle

    private lateinit var edMarcaAdd: EditText
    private lateinit var edModeloAdd: EditText
    private lateinit var edMatriculaAdd: EditText
    private lateinit var vehicleAddProgressBar: ProgressBar

    private var mClient: MobileServiceClient? = null

    private var vehicleTable: MobileServiceTable<Vehicle>? = null
    private var driverTable: MobileServiceTable<Driver>? = null

    private lateinit var loadFragment: LoadFragments


    private lateinit var driver: Driver
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_add, container, false)
        auth = FirebaseAuth.getInstance()

        driver = Driver()

        loadFragment = activity as LoadFragments
        vehSpinner = view.findViewById(R.id.spinnerVehicle)
        edMarcaAdd = view.findViewById(R.id.edMarcaAdd)
        edModeloAdd = view.findViewById(R.id.edModeloAdd)
        edMatriculaAdd = view.findViewById(R.id.edMatriculaAdd)

        vehicleAddProgressBar = view.findViewById(R.id.vehicleAddProgressBar)

        vehSpinner.onItemSelectedListener = this

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val vehAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.vehicles_array))
        // Set layout to use when the list of choices appear
        vehAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        vehSpinner.adapter = vehAdapter

        vehicle = Vehicle()

        continueB = view.findViewById(R.id.buttonContinue1)
        continueB.setOnClickListener {
            //When client press the add button, create a vehicle object and insert it into the DDBB
            vehicleAddProgressBar.visibility = View.VISIBLE
            vehicle.id = ""
            vehicle.brand = edMarcaAdd.text.toString()
            vehicle.model = edModeloAdd.text.toString()
            vehicle.state = "active"
            vehicle.licensePlate = edMatriculaAdd.text.toString()
            vehicle.type = vehSpinner.selectedItem.toString()

            try {
                // Create the client instance, using the provided mobile app URL.
                mClient = AzureClient.getInstance(context!!).getClient()



                mClient!!.setAndroidHttpClientFactory {
                    val client = OkHttpClient()
                    client.readTimeoutMillis()
                    client.writeTimeoutMillis()
                    client
                }

                vehicleTable = mClient!!.getTable(Vehicle::class.java)
                driverTable = mClient!!.getTable(Driver::class.java)

                doAsync {
                    val bundle = Bundle()
                    val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                    for (driver in resultDriverQuery) {
                        vehicle.idDriver = driver.id
                        vehicleTable!!.insert(vehicle)
                        uiThread {
                            vehicleAddProgressBar.visibility = View.GONE
                        }
                        break
                    }
                    loadFragment.loadFragment(8, bundle)

                }

            } catch (e: MalformedURLException) {
                AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
            } catch (e: java.lang.Exception){
                AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
            }
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
    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }


}
