package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.*
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.BankProfile
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class BankProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var  dateSpinner: Spinner
    private lateinit var bBankProfile: Button

    private lateinit var edNumberCard: EditText
    private lateinit var edNameCard: EditText
    private lateinit var edSecurityNumber: EditText

    private var mClient: MobileServiceClient? = null

    private var bankProfileTable: MobileServiceTable<BankProfile>? = null

    private var bankP: BankProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_bank_profile, container, false)

        dateSpinner = view.findViewById(R.id.date_spinner)
        bBankProfile = view.findViewById(R.id.bBankProfile)
        edNumberCard = view.findViewById(R.id.edNumberCard)
        edNameCard = view.findViewById(R.id.edNameCard)
        edSecurityNumber = view.findViewById(R.id.edSecurityNumber)


        dateSpinner.onItemSelectedListener = this
        // Create an ArrayAdapter using a simple spinner layout and languages array
        val dateAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.date_array))
        // Set layout to use when the list of choices appear
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        dateSpinner.adapter = dateAdapter

        bBankProfile.setOnClickListener {
            doAsync {
                bankProfileTable!!.update(bankP).get()
            }
        }

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
            doAsync {

                val resultQuery = bankProfileTable!!.execute().get()
                for (bankProfile in resultQuery){

                    bankP = bankProfile
                    uiThread {
                        edNumberCard.setText(bankProfile.numberCard)
                        edNameCard.setText(bankProfile.nameCard)
                        edSecurityNumber.setText(bankProfile.securityNumber)
                        dateSpinner.setSelection(dateAdapter.getPosition(bankProfile.dateCard))

                    }

                }


            }



        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }

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

            Toast.makeText(context, dateSpinner.selectedItem.toString(), Toast.LENGTH_LONG).show()

        }
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }
}
