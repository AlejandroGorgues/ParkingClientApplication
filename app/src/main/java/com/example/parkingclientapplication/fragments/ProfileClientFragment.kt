package com.example.parkingclientapplication.fragments


import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.*

import com.example.parkingclientapplication.R
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.fragment_profile_client.*
import android.widget.ProgressBar
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import android.app.AlertDialog
import android.support.design.widget.TextInputEditText
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.model.Driver
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.coroutines.withContext
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class ProfileClientFragment : Fragment() {

    private var mClient: MobileServiceClient? = null

    private lateinit var edPassword: EditText
    private lateinit var edEmail: EditText
    private lateinit var edName: EditText
    private lateinit var bUpdate: Button

    private var driverTable: MobileServiceTable<Driver>? = null
    private lateinit var driver: Driver


    /**
     * Progress spinner to use for table operations
     */

    private val mProgressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_profile_client, container, false)
        edPassword = view.findViewById(R.id.edPassword)
        edEmail = view.findViewById(R.id.edEmail)
        edName = view.findViewById(R.id.edName)
        bUpdate = view.findViewById(R.id.buttonUpdate)
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

                val resultQuery = driverTable!!.where().field("username").eq("agorgues").execute().get()
                for (profile in resultQuery){
                    driver = profile
                    uiThread {
                        edName.setText(profile.username)
                        edPassword.setText(profile.password)
                        edEmail.setText(profile.email)
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
            doAsync {
                driverTable!!.update(driver).get()
            }
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


    /*private class ProgressFilter : ServiceFilter {

        override fun handleRequest(
            request: ServiceFilterRequest,
            nextServiceFilterCallback: NextServiceFilterCallback
        ): ListenableFuture<ServiceFilterResponse> {

            val resultFuture = SettableFuture.create<ServiceFilterResponse>()


            activity.runOnUiThread { mProgressBar.visibility = ProgressBar.VISIBLE }

            val future = nextServiceFilterCallback.onNext(request)

            Futures.addCallback(future, object : FutureCallback<ServiceFilterResponse> {
                override fun onFailure(e: Throwable) {
                    resultFuture.setException(e)
                }

                override fun onSuccess(response: ServiceFilterResponse?) {
                    activity.runOnUiThread(Runnable { mProgressBar.visibility = ProgressBar.GONE })

                    resultFuture.set(response)
                }
            })

            return resultFuture
        }
    }*/
}

