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
import com.example.parkingclientapplication.model.Reservation
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
    private var reservations = ArrayList<Reservation>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_reservation_list_client, container, false)
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
            doAsync {

                val resultQuery = reservationTable!!.execute().get()
                for (reservation in resultQuery){
                    reservations.add(reservation)
                    uiThread {
                        reservAdapter.notifyDataSetChanged()
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
            //Log.e("aqui", reservations[opt].timeReservation.toString())
            bundle.putParcelable("reservation", reservations[opt])
            loadFragment.loadFragment(6, bundle)


        })
        inicializarReciclerView()
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

    private fun inicializarReciclerView(){
        reservRecyclerView.adapter = reservAdapter
        reservRecyclerView.layoutManager = LinearLayoutManager(activity)
        reservRecyclerView.itemAnimator = DefaultItemAnimator()
    }

}
