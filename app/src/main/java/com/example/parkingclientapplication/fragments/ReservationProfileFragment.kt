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
import com.example.parkingclientapplication.model.ParkingLot
import com.example.parkingclientapplication.model.Reservation
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import java.net.MalformedURLException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ReservationProfileFragment : Fragment() {
    private lateinit var edUserReserv: EditText
    private lateinit var edParkingReserv: EditText
    private lateinit var edTarjetaReserv: EditText
    private lateinit var edMatriculaReserv: EditText
    private lateinit var edTiempoReserv: EditText
    private lateinit var edFechaReserv: EditText
    private lateinit var edEstadoReserv: EditText
    private lateinit var edPrecioReserv: EditText
    private var parkingLotTable: MobileServiceTable<ParkingLot>? = null
    private var mClient: MobileServiceClient? = null

    private lateinit var bGuide: Button

    private lateinit var loadFragments: LoadFragments

    private lateinit var reservation: Reservation


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
        edTiempoReserv = view.findViewById(R.id.edTiempoReserv)
        edFechaReserv = view.findViewById(R.id.edFechaReserv)
        edEstadoReserv = view.findViewById(R.id.edEstadoReserv)
        edPrecioReserv = view.findViewById(R.id.edPrecioReserv)

        reservation = arguments!!.getParcelable("reservation")!!

        edUserReserv.setText("Usuario")
        edParkingReserv.setText("Parking reserva")
        edTarjetaReserv.setText("Tarjeta usuario")
        edMatriculaReserv.setText(reservation.licensePlate)
        edTiempoReserv.setText(reservation.timeActive.toString())
        edFechaReserv.setText(trimDate(reservation.dateReservation.toString()))
        edEstadoReserv.setText(reservation.state)
        edPrecioReserv.setText(reservation.expensesActive.toString())

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

            parkingLotTable = mClient!!.getTable(ParkingLot::class.java)
            doAsync {

                val resultParkingLotQuery = parkingLotTable!!.where().field("id").eq(reservation.idParkingLot).execute().get()
                for (parkingLot in resultParkingLotQuery) {
                    bundle.putParcelable("parkingLot", parkingLot)
                }

            }

        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }



        bGuide.setOnClickListener {
            loadFragments.loadFragment(3, bundle)
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


}
