package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Vehicle
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import java.net.MalformedURLException

class VehicleModifiedFragment : Fragment() {

    private lateinit var edTipoVehiculo: EditText
    private lateinit var edMarcaVehiculo: EditText
    private lateinit var edModeloVehiculo: EditText
    private lateinit var edMatriculaVehiculo: EditText

    private lateinit var bModifiedVehiculo: Button

    private lateinit var vehicle: Vehicle

    private var mClient: MobileServiceClient? = null

    private var vehicleTable: MobileServiceTable<Vehicle>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_modified, container, false)

        edTipoVehiculo = view.findViewById(R.id.edTipoVehiculo)
        edMarcaVehiculo = view.findViewById(R.id.edMarcaVehiculo)
        edModeloVehiculo = view.findViewById(R.id.edModeloVehiculo)
        edMatriculaVehiculo = view.findViewById(R.id.edMatriculaVehiculo)

        bModifiedVehiculo = view.findViewById(R.id.bModifiedVehiculo)

        vehicle = arguments!!.getParcelable("vehiculo")!!

        edTipoVehiculo.setText(vehicle.type)
        edMarcaVehiculo.setText(vehicle.brand)
        edModeloVehiculo.setText(vehicle.model)
        edMatriculaVehiculo.setText(vehicle.licensePlate)

        bModifiedVehiculo.setOnClickListener {
            try {
                vehicle.brand = edMarcaVehiculo.text.toString()
                vehicle.model = edModeloVehiculo.text.toString()
                vehicle.licensePlate = edMatriculaVehiculo.text.toString()
                // Create the client instance, using the provided mobile app URL.
                mClient = AzureClient.getInstance(context!!).getClient()



                mClient!!.setAndroidHttpClientFactory {
                    val client = OkHttpClient()
                    client.readTimeoutMillis()
                    client.writeTimeoutMillis()
                    client
                }

                vehicleTable = mClient!!.getTable(Vehicle::class.java)

                doAsync {
                    vehicleTable!!.update(vehicle)
                }

            } catch (e: MalformedURLException) {
                AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
            } catch (e: java.lang.Exception){
                AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
            }
        }
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
