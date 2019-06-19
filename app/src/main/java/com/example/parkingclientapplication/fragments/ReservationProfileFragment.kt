package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import java.net.MalformedURLException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReservationProfileFragment : Fragment() {
    private lateinit var edUserReserv: EditText
    private lateinit var edParkingReserv: EditText
    private lateinit var edTarjetaReserv: EditText
    private lateinit var edMatriculaReserv: EditText
    private lateinit var edFechaReserv: EditText
    private lateinit var edEstadoReserv: EditText
    private lateinit var edPrecioReserv: EditText

    private var parkingLotTable: MobileServiceTable<ParkingLot>? = null
    private var driverTable: MobileServiceTable<Driver>? = null
    private var bankProfileTable: MobileServiceTable<BankProfile>? = null
    private var parkingTable: MobileServiceTable<Parking>? = null

    private var parkingLots: ArrayList<ParkingLot>? = null

    private lateinit var auth: FirebaseAuth

    private var mClient: MobileServiceClient? = null

    private lateinit var bGuide: Button

    private lateinit var loadFragments: LoadFragments

    private lateinit var reservation: Reservation
    private lateinit var parkingLot: ParkingLot


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_reservation_profile, container, false)
        loadFragments = activity as LoadFragments
        edUserReserv = view.findViewById(R.id.edUserReserv)
        edParkingReserv = view.findViewById(R.id.edParkingReserv)
        edTarjetaReserv = view.findViewById(R.id.edTarjetaReserv)
        edMatriculaReserv = view.findViewById(R.id.edMatriculaReserv)
        edFechaReserv = view.findViewById(R.id.edFechaReserv)
        edEstadoReserv = view.findViewById(R.id.edEstadoReserv)
        edPrecioReserv = view.findViewById(R.id.edPrecioReserv)

        reservation = arguments!!.getParcelable("reservation")!!

        edUserReserv.setText("")
        edParkingReserv.setText(reservation.nameParking)
        edTarjetaReserv.setText("")
        edMatriculaReserv.setText(reservation.licensePlate)
        edFechaReserv.setText(trimDate(reservation.dateReservation.toString()))
        edEstadoReserv.setText(reservation.state)
        edPrecioReserv.setText(reservation.expensesActive.toString())

        parkingLots = ArrayList()

        bGuide = view.findViewById(R.id.guideAccess)
        val bundle = Bundle()
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }


            driverTable = mClient!!.getTable(Driver::class.java)
            bankProfileTable = mClient!!.getTable(BankProfile::class.java)
            doAsync {

                val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                for (driverAux in resultDriverQuery){
                    val resultBankProfileQuery = bankProfileTable!!.where().field("idDriver").eq(driverAux.id).execute().get()
                    for (bankProfileAux in resultBankProfileQuery){
                        runOnUiThread {
                            edUserReserv.setText(driverAux.username)
                            edTarjetaReserv.setText(bankProfileAux.numberCard)
                        }
                    }

                }
            }

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }



        bGuide.setOnClickListener {
            parkingLotTable = mClient!!.getTable(ParkingLot::class.java)
            parkingTable = mClient!!.getTable(Parking::class.java)

            doAsync {

                val resultParkingQuery = parkingTable!!.where().field("nameParking").eq(reservation.nameParking).execute().get()
                for (parkingAux in resultParkingQuery){
                    bundle.putParcelable("parking", parkingAux)
                    bundle.putParcelable("reservation", reservation)
                    runOnUiThread {
                        loadFragments.loadFragment(7, bundle)
                    }
                }


            }

        }

        return view
    }

    private fun trimDate(dateString:String): String{
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
        var dateReservation: Date? = null
        try {
            dateReservation = df.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return DateFormat.getInstance().format(dateReservation)
    }


    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }


}
