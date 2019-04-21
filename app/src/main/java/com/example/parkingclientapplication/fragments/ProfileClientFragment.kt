package com.example.parkingclientapplication.fragments


import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.*

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.model.Profile
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import kotlinx.android.synthetic.main.fragment_profile_client.*
import android.widget.ProgressBar
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import android.app.AlertDialog
import java.net.MalformedURLException


class ProfileClientFragment : Fragment() {

    private var mClient: MobileServiceClient? = null

    /**
     * Progress spinner to use for table operations
     */

    private val mProgressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_profile_client, container, false)
        try {
        // Create the client instance, using the provided mobile app URL.
        mClient = MobileServiceClient(
                "https://clientmobileparking.azurewebsites.net",
        context)

        mClient!!.setAndroidHttpClientFactory {
            val client = OkHttpClient()
            client.readTimeoutMillis()
            client.writeTimeoutMillis()
            client
        }

        doAsync {
            val profileTable = mClient!!.getTable(Profile::class.java).execute().get()
            for (profile in profileTable){
                profileName.text = Editable.Factory.getInstance().newEditable(profile.username)
                profilePassword.text = Editable.Factory.getInstance().newEditable(profile.password)
                profileEmail.text = Editable.Factory.getInstance().newEditable(profile.email)
            }
        }
        } catch (e: MalformedURLException) {
            createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            createAndShowDialog(e, "Error")
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
    /**
     * Creates a dialog and shows it
     *
     * @param exception
     * The exception to show in the dialog
     * @param title
     * The dialog title
     */
    private fun createAndShowDialog(exception: Exception, title: String) {
        var ex: Throwable = exception
        if (exception.cause != null) {
            ex = exception.cause!!
        }
        ex.message?.let { createAndShowDialog(it, title) }
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     * The dialog message
     * @param title
     * The dialog title
     */
    private fun createAndShowDialog(message: String, title: String) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(message)
        builder.setTitle(title)
        builder.create().show()
    }
}

