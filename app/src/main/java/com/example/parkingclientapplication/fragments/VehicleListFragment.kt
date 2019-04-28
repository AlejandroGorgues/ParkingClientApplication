package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.VehicleListAdapter
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.text.Layout
import android.util.Log
import android.view.*
import android.widget.ImageButton
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.interfaces.UpdateVehicleList
import com.example.parkingclientapplication.model.Driver
import com.example.parkingclientapplication.model.Reservation
import com.example.parkingclientapplication.model.Vehicle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException

class VehicleListFragment : Fragment(), UpdateVehicleList {



    private lateinit var vehAdapter: RecyclerView.Adapter<*>
    private lateinit var vehRecyclerView: RecyclerView
    private var mClient: MobileServiceClient? = null

    private var vehicles = ArrayList<Vehicle>()
    private var vehicleTable: MobileServiceTable<Vehicle>? = null
    private var driverTable: MobileServiceTable<Driver>? = null


    private lateinit var driver: Driver
    private lateinit var auth: FirebaseAuth

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_list, container, false)
        auth = FirebaseAuth.getInstance()

        driver = Driver()
        loadFragment = activity as LoadFragments
        vehRecyclerView = view!!.findViewById(R.id.client_vehicle_recycler_view)
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }
            obtainTable()

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }

        vehAdapter = VehicleListAdapter(vehicles, context!!, vehicleTable!!, this)
        (vehAdapter as VehicleListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = vehRecyclerView.getChildAdapterPosition(v)
            val bundle = Bundle()
            //Log.e("aqui", reservations[opt].timeReservation.toString())
            bundle.putParcelable("vehiculo", vehicles[opt])
            loadFragment.loadFragment(2, bundle)


        })

        val fab = view.findViewById(R.id.newVehicle) as FloatingActionButton
        fab.setOnClickListener {
            // Click action
            val bundle = Bundle()
            loadFragment.loadFragment(4, bundle)
        }
        inicializarReciclerView()
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return view
    }

    private fun obtainTable() {
        vehicleTable = mClient!!.getTable(Vehicle::class.java)
        driverTable = mClient!!.getTable(Driver::class.java)
        doAsync {
            val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
            for (driver in resultDriverQuery){
                val resultVehicleQuery = vehicleTable!!.where().field("idDriver").eq(driver.id).execute().get()
                for (vehicle in resultVehicleQuery) {
                    vehicles.add(vehicle)
                    uiThread {
                        vehAdapter.notifyDataSetChanged()
                    }
                }
            }



        }
    }

    private fun deleteVehicleTable(vehicle:Vehicle) {
        vehicleTable!!.delete(vehicle)
        vehicles.remove(vehicle)
        vehAdapter.notifyDataSetChanged()

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

    private fun inicializarReciclerView(){
        vehRecyclerView.adapter = vehAdapter
        vehRecyclerView.layoutManager = LinearLayoutManager(activity)
        vehRecyclerView.itemAnimator = DefaultItemAnimator()
    }

    override fun updateList(vehicle: Vehicle) {
        deleteVehicleTable(vehicle)

    }

    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }




}
