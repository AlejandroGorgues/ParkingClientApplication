package com.example.parkingclientapplication

import android.app.AlertDialog
import android.content.Context
import com.microsoft.windowsazure.mobileservices.MobileServiceClient

class AzureClient(context: Context) {
    private var contex: Context? = null
    init {
        this.contex = context
    }

    fun getClient(): MobileServiceClient{
        return MobileServiceClient("https://clientmobileparking.azurewebsites.net", contex)
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     * The exception to show in the dialog
     * @param title
     * The dialog title
     */
    fun createAndShowDialog(exception: Exception, title: String) {
        var ex: Throwable = exception
        if (exception.cause != null) {
            ex = exception.cause!!
        }
        ex.message?.let { createAndShowDialog(it, title, contex!!) }
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     * The dialog message
     * @param title
     * The dialog title
     */
    private fun createAndShowDialog(message: String, title: String, context: Context) {
        val builder = AlertDialog.Builder(context)

        builder.setMessage(message)
        builder.setTitle(title)
        builder.create().show()
    }
    companion object : SingletonHolder<AzureClient, Context>(::AzureClient)

}