package com.example.parkingclientapplication.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.*
import com.example.parkingclientapplication.R
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import android.widget.ProgressBar
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.android.synthetic.main.fragment_profile_client.*
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class ProfileClientFragment : Fragment() {

    private var mClient: MobileServiceClient? = null

    private lateinit var edPassword: EditText
    private lateinit var edEmail: EditText
    private lateinit var edName: EditText
    private lateinit var bUpdate: Button
    private lateinit var prbProfile: ProgressBar

    private var driverTable: MobileServiceTable<Driver>? = null
    private lateinit var driver: Driver
    private lateinit var driverAux: Driver

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_profile_client, container, false)
        auth = FirebaseAuth.getInstance()
        edPassword = view.findViewById(R.id.edPassword)
        edEmail = view.findViewById(R.id.edEmail)
        edName = view.findViewById(R.id.edName)
        bUpdate = view.findViewById(R.id.buttonUpdate)
        prbProfile = view.findViewById(R.id.profileProgressBar)
        prbProfile.visibility = View.VISIBLE

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
            doAsync {
                val resultQuery = driverTable!!.where().field("email").eq(getEmail(auth.currentUser!!)).execute().get()
                for (profile in resultQuery){
                    driver = profile
                    driverAux = profile
                    uiThread {
                        edName.setText(driver.username)
                        edPassword.setText(driver.password)
                        edEmail.setText(driver.email)
                        prbProfile.visibility = View.INVISIBLE
                    }


                }
            }



        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }

        edName.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    driver.username = s.toString()
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

        edEmail.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    driver.email = s.toString()
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

        edPassword.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    driver.password = s.toString()
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

        bUpdate.setOnClickListener {
            profileProgressBar.visibility = View.VISIBLE
            driver.email = edEmail.text.toString()
            driver.password = edPassword.text.toString()
            driver.username = edName.text.toString()
            doAsync {
                if(driver.email != driverAux.email){
                    user.updateEmail(driver.email!!)
                        .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Log.d(TAG, "User email address updated.")
                        }
                    }
                }
                if(driver.password != driverAux.password){
                    user.updatePassword(driverAux.password!!)
                        .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Log.d(TAG, "User password updated.")
                        }
                    }
                }
                driverTable!!.update(driver).get()
                profileProgressBar.visibility = View.INVISIBLE
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        user = auth.currentUser!!

    }

    private fun getEmail(user: FirebaseUser):String{
        user.let {
            // Name, email address, and profile photo Url
            return user.email!!
        }

    }
}

