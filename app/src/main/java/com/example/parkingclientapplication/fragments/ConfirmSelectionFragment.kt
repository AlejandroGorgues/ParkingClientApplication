package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.yesButton
import java.net.MalformedURLException
import kotlin.collections.ArrayList

class ConfirmSelectionFragment : Fragment() {
    private lateinit var buttonConfirm: Button
    private lateinit var edUsernameConfirm: EditText
    private lateinit var edNumberCardConfirm: EditText
    private lateinit var edVehicleConfirm: EditText
    private lateinit var edParkingConfirm: EditText

    private var mClient: MobileServiceClient? = null

    private var reservationTable: MobileServiceTable<Reservation>? = null
    private var parkingLotTable: MobileServiceTable<ParkingLot>? = null
    private var driverTable: MobileServiceTable<Driver>? = null


    private lateinit var driver: Driver
    private lateinit var auth: FirebaseAuth

    private lateinit var parking: Parking
    private lateinit var vehicle: Vehicle
    private lateinit var reservation: Reservation
    private lateinit var parkingLot: ParkingLot

    private lateinit var parkingLots: ArrayList<ParkingLot>

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirm_selection, container, false)
        val bundle = Bundle()
        loadFragment = activity as LoadFragments
        buttonConfirm = view.findViewById(R.id.buttonConfirmation)
        edUsernameConfirm = view.findViewById(R.id.edUsernameConfirm)
        edNumberCardConfirm = view.findViewById(R.id.edNumberCardConfirm)
        edVehicleConfirm = view.findViewById(R.id.edVehicleConfirm)
        edParkingConfirm = view.findViewById(R.id.edParkingConfirm)

        parking = arguments!!.getParcelable("parkingSelected")!!
        vehicle = arguments!!.getParcelable("vehicleSelected")!!

        edUsernameConfirm.setText("Username")
        edNumberCardConfirm.setText("1234-5678-9012-3456")
        edVehicleConfirm.setText(vehicle.licensePlate)
        edParkingConfirm.setText(parking.nameParking)

        auth = FirebaseAuth.getInstance()

        driver = Driver()
        reservation = Reservation()
        parkingLot = ParkingLot()
        parkingLots = ArrayList()

        buttonConfirm.setOnClickListener {
            try {

                // Create the client instance, using the provided mobile app URL.
                mClient = AzureClient.getInstance(context!!).getClient()



                mClient!!.setAndroidHttpClientFactory {
                    val client = OkHttpClient()
                    client.readTimeoutMillis()
                    client.writeTimeoutMillis()
                    client
                }

                //Set a reservation profile to be uploaded to the database
                reservation.id = ""
                reservation.licensePlate = vehicle.licensePlate
                reservation.model = vehicle.model
                reservation.brand = vehicle.brand
                reservation.expensesActive = parking.price
                reservation.dateReservation = ""
                reservation.timeActive = 0
                reservation.state = "open"

                reservationTable = mClient!!.getTable(Reservation::class.java)
                parkingLotTable = mClient!!.getTable(ParkingLot::class.java)
                driverTable = mClient!!.getTable(Driver::class.java)
                mClient!!.

                doAsync {

                    //Obtain all parking lots related to the parking selected
                    val resultParkingLotQuery = parkingLotTable!!.where().field("idParking").eq(parking.id).execute().get()
                    for (parkingLot in resultParkingLotQuery){
                        parkingLots.add(parkingLot)
                    }

                    //Obtain a random parking lot that it's currently free
                    do {
                        parkingLot = parkingLots[(0 until parkingLots.size).random()]
                    }while (parkingLot.stateLot != "free")

                    //Obtain the driver profile
                    val resultDriverQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                    for (driver in resultDriverQuery){
                        reservation.idDriver = driver.id
                    }

                    //Insert the reservation to the database
                    reservation.idParkingLot = parkingLot.id
                    reservationTable!!.insert(reservation)
                    runOnUiThread {
                        alert("Su número de plaza asociado es "+parkingLot.position+"\n Para obtener todos los datos de la reserva, puede acceder a Mis Reservas para ver todas") {
                            title = "Información"
                            yesButton { loadFragment.loadFragment(2, bundle) }
                        }.show()
                    }
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

    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }
}
