package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Visibility
import android.view.*
import android.widget.*
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.BankProfile
import com.example.parkingclientapplication.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.android.synthetic.main.fragment_bank_profile.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class BankProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var dateSpinner: Spinner
    private lateinit var bUpdateBankProfile: Button
    private lateinit var bCreateBankProfile: Button
    private lateinit var bDeleteBankProfile: Button
    private lateinit var prbBank: ProgressBar

    private lateinit var edNumberCard: EditText
    private lateinit var edNameCard: EditText
    private lateinit var edSecurityNumber: EditText

    private var mClient: MobileServiceClient? = null

    private var bankProfileTable: MobileServiceTable<BankProfile>? = null
    private var driverTable: MobileServiceTable<Driver>? = null

    private var bankP: BankProfile? = null
    private lateinit var driverF: Driver
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_bank_profile, container, false)

        dateSpinner = view.findViewById(R.id.date_spinner)
        bUpdateBankProfile = view.findViewById(R.id.bUpdateBankProfile)
        bCreateBankProfile = view.findViewById(R.id.bCreateBankProfile)
        bDeleteBankProfile = view.findViewById(R.id.bDeleteBankProfile)

        edNumberCard = view.findViewById(R.id.edNumberCard)
        edNameCard = view.findViewById(R.id.edNameCard)
        edSecurityNumber = view.findViewById(R.id.edSecurityNumber)
        prbBank = view.findViewById(R.id.bankProgressBar)

        auth = FirebaseAuth.getInstance()
        driverF = Driver()
        bankP = BankProfile()
        dateSpinner.onItemSelectedListener = this
        // Create an ArrayAdapter using a simple spinner layout and languages array
        val dateAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.date_array))
        // Set layout to use when the list of choices appear
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        dateSpinner.adapter = dateAdapter

        bUpdateBankProfile.setOnClickListener {
            doAsync {
                prbBank.visibility = View.VISIBLE
                bankProfileTable!!.update(bankP).get()
                prbBank.visibility = View.INVISIBLE
            }
        }

        bCreateBankProfile.setOnClickListener {
            doAsync {
                prbBank.visibility = View.VISIBLE
                bankP!!.securityNumber = edSecurityNumber.text.toString()
                bankP!!.dateCard = dateSpinner.selectedItem.toString()
                bankP!!.numberCard = edNumberCard.text.toString()
                bankP!!.nameCard = edNameCard.text.toString()
                bankP!!.id = ""
                bankP!!.idDriver = driverF.id

                bankProfileTable!!.insert(bankP).get()
                prbBank.visibility = View.INVISIBLE
            }

        }
        bDeleteBankProfile.setOnClickListener {
            bankProfileTable!!.delete(bankP)
        }

        getBankData(dateAdapter)

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

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        if(position == 0)
        {

            //Do nothing.
        }
        else{

            //Toast.makeText(context, dateSpinner.selectedItem.toString(), Toast.LENGTH_LONG).show()

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
    private fun getBankData(dateAdapter: ArrayAdapter<String>) {
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()

            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }

            bankProfileTable = mClient!!.getTable(BankProfile::class.java)
            driverTable = mClient!!.getTable(Driver::class.java)
            prbBank.visibility = View.VISIBLE
            doAsync {

                val resultDriverQuery =
                    driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                for (driver in resultDriverQuery) {
                    driverF = driver
                    val resultBankQuery = bankProfileTable!!.where().field("idDriver").eq(driver.id).execute().get()
                    if (resultBankQuery.size == 0) {
                        bUpdateBankProfile.visibility = View.INVISIBLE
                        bDeleteBankProfile.visibility = View.INVISIBLE
                    } else {
                        bCreateBankProfile.visibility = View.INVISIBLE
                        for (bankProfile in resultBankQuery) {

                            bankP = bankProfile
                            uiThread {
                                edNumberCard.setText(bankProfile.numberCard)
                                edNameCard.setText(bankProfile.nameCard)
                                edSecurityNumber.setText(bankProfile.securityNumber)
                                dateSpinner.setSelection(dateAdapter.getPosition(bankProfile.dateCard))
                                prbBank.visibility = View.INVISIBLE

                            }

                        }
                    }
                }
            }
        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(
                Exception("There was an error creating the Mobile Service. Verify the URL"),
                "Error"
            )
        } catch (e: java.lang.Exception) {
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }
    }
}
