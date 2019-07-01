package com.example.parkingclientapplication

import android.app.AlertDialog
import android.content.Context
import com.microsoft.windowsazure.mobileservices.MobileServiceClient

class AzureClient(context: Context) {
    private var context: Context? = null
    init {
        this.context = context
    }

    fun getClient(): MobileServiceClient{
        return MobileServiceClient("https://clientmobileparking.azurewebsites.net", context)
    }

    /*
     * Creates a dialog and shows it
     */
    fun createAndShowDialog(exception: Exception, title: String) {
        var ex: Throwable = exception
        if (exception.cause != null) {
            ex = exception.cause!!
        }
        ex.message?.let { createAndShowDialog(it, title, context!!) }
    }

    /*
     * Creates a dialog and shows it
     */
    private fun createAndShowDialog(message: String, title: String, context: Context) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(message)
        builder.setTitle(title)
        builder.create().show()
    }

    //Create the singleton holder that expect to get an AzureClient
    companion object : SingletonHolder<AzureClient, Context>(::AzureClient)

}