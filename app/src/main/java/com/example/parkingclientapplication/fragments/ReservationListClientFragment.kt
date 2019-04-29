package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.ReservationListAdapter
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Driver
import com.example.parkingclientapplication.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException

class ReservationListClientFragment() : Fragment() {

    private lateinit var reservAdapter: RecyclerView.Adapter<*>
    private lateinit var reservRecyclerView: RecyclerView

    private lateinit var loadFragment: LoadFragments

    private var mClient: MobileServiceClient? = null

    private var reservationTable: MobileServiceTable<Reservation>? = null
    private var driverTable: MobileServiceTable<Driver>? = null
    private var reservations = ArrayList<Reservation>()

    private lateinit var driver: Driver
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_reservation_list_client, container, false)
        auth = FirebaseAuth.getInstance()

        driver = Driver()
        loadFragment = activity as LoadFragments
        reservRecyclerView = view.findViewById(R.id.client_reservation_recycler_view)
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }

            reservationTable = mClient!!.getTable(Reservation::class.java)
            driverTable = mClient!!.getTable(Driver::class.java)
            doAsync {

                val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                for (driver in resultDriverQuery) {
                    val resultReservQuery = reservationTable!!.where().field("idDriver").eq(driver.id).execute().get()
                    for (reservation in resultReservQuery) {

                        reservations.add(reservation)
                        uiThread {
                            reservAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }

        reservAdapter = ReservationListAdapter(reservations, context!!)
        (reservAdapter as ReservationListAdapter).setOnClickListener(View.OnClickListener { v ->
            val opt = reservRecyclerView.getChildAdapterPosition(v)
            val bundle = Bundle()

            bundle.putParcelable("reservation", reservations[opt])
            loadFragment.loadFragment(6, bundle)


        })
        inicializarReciclerView()
        // Inflate the layout for this fragment
        return view
    }

    private fun inicializarReciclerView(){
        reservRecyclerView.adapter = reservAdapter
        reservRecyclerView.layoutManager = LinearLayoutManager(activity)
        reservRecyclerView.itemAnimator = DefaultItemAnimator()
    }


    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }

}
