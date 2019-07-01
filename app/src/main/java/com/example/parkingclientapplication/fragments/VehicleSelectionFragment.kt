package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.VehicleSelectionAdapter
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


class VehicleSelectionFragment : Fragment() {

    private lateinit var vehAdapter: RecyclerView.Adapter<*>
    private lateinit var vehRecyclerView: RecyclerView

    private lateinit var loadFragment: LoadFragments

    private var mClient: MobileServiceClient? = null

    private var vehicleTable: MobileServiceTable<Vehicle>? = null
    private var driverTable: MobileServiceTable<Driver>? = null

    private lateinit var driver: Driver
    private lateinit var auth: FirebaseAuth
    private lateinit var vehicle: Vehicle
    private var vehicles = ArrayList<Vehicle>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_selection, container, false)

        auth = FirebaseAuth.getInstance()

        driver = Driver()

        loadFragment = activity as LoadFragments
        vehRecyclerView = view!!.findViewById(R.id.client_vehicle_selection_recycler_view)
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }


            //Obtain all the vehicles avaliable and related to the client
            obtainTable()

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }
        vehAdapter = VehicleSelectionAdapter(vehicles)
        (vehAdapter as VehicleSelectionAdapter).setOnClickListener(View.OnClickListener { v ->
            val bundle = Bundle()
            val opt = vehRecyclerView.getChildAdapterPosition(v)
            vehicle = vehicles[opt]
            bundle.putParcelable("vehicleSelected", vehicle)
            bundle.putParcelable("parkingSelected", arguments!!.getParcelable("parking")!!)
            loadFragment.loadFragment(1, bundle)


        })

        inicializarReciclerView()
        // Inflate the layout for this fragment
        return view
    }

    private fun inicializarReciclerView(){
        vehRecyclerView.adapter = vehAdapter
        vehRecyclerView.layoutManager = LinearLayoutManager(activity)
        vehRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun obtainTable() {
        driverTable = mClient!!.getTable(Driver::class.java)
        vehicleTable = mClient!!.getTable(Vehicle::class.java)
        doAsync {

            val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
            for (driver in resultDriverQuery) {
                val resultQuery =
                    vehicleTable!!.where().field("idDriver").eq(driver.id).execute().get()
                for (vehicle in resultQuery) {
                    vehicles.add(vehicle)
                    uiThread {
                        vehAdapter.notifyDataSetChanged()
                    }
                }
            }

        }
    }

    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }

}
