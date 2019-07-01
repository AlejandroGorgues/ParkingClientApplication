package com.example.parkingclientapplication.fragments


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.parkingclientapplication.AzureClient

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.activities.ClientMapActivity
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Driver
import com.google.firebase.auth.FirebaseAuth
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException


class RegisterClientFragment : Fragment() {

    private lateinit var buttonAccess: Button
    private lateinit var txtLogin: TextView
    private lateinit var auth: FirebaseAuth

    private lateinit var edUsernameRegister: EditText
    private lateinit var edPasswordRegister: EditText
    private lateinit var edEmailRegister: EditText

    private lateinit var tilPasswordRegister: TextInputLayout
    private lateinit var tilEmailRegister: TextInputLayout

    private lateinit var registerProgressBar: ProgressBar

    private var mClient: MobileServiceClient? = null

    private var driverTable: MobileServiceTable<Driver>? = null
    private lateinit var driver: Driver

    private lateinit var loadFragments: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register_client, container, false)
        auth = FirebaseAuth.getInstance()
        buttonAccess = view!!.findViewById(R.id.buttonAccess)
        txtLogin = view.findViewById(R.id.txtLogin)
        edUsernameRegister = view.findViewById(R.id.edUsernameRegister)
        edPasswordRegister = view.findViewById(R.id.edPasswordRegister)
        edEmailRegister = view.findViewById(R.id.edEmailRegister)
        tilPasswordRegister = view.findViewById(R.id.tilPassword)
        tilEmailRegister = view.findViewById(R.id.tilEmail)

        registerProgressBar = view.findViewById(R.id.registerProgressBar)

        edEmailRegister.setText("a@gmail.com")
        edUsernameRegister.setText("a")
        edPasswordRegister.setText("123456")

        loadFragments = activity as LoadFragments

        driver = Driver()

        edEmailRegister.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    esCorreoValido(s.toString())
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })

        buttonAccess.setOnClickListener {
            validarDatos()

        }
        txtLogin.setOnClickListener {
            val bundle = Bundle()
            loadFragments.loadFragment(2, bundle)
        }
        // Inflate the layout for this fragment
        return view
    }

    private fun createDriver(){
        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(context!!).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }

            driver.id = ""
            driver.username = edUsernameRegister.text.toString()
            driver.password = edPasswordRegister.text.toString()
            driver.email = edEmailRegister.text.toString()

            driverTable = mClient!!.getTable(Driver::class.java)
            doAsync {

                driverTable!!.insert(driver)
                uiThread {
                    val intent = Intent(activity, ClientMapActivity::class.java)
                    startActivity(intent)
                }
            }



        } catch (e: MalformedURLException) {
            AzureClient.getInstance(context!!).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(context!!).createAndShowDialog(e, "Error")
        }
    }

    private fun validarDatos(){
        if (esPasswordValido(tilPasswordRegister.editText!!.text.toString())){
            registerProgressBar.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(edEmailRegister.text.toString(), edPasswordRegister.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(TAG, "createUserWithEmail:success")

                        createDriver()
                        registerProgressBar.visibility = View.GONE
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }

                }
        }
    }

    private fun esCorreoValido(correo: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilEmailRegister.error = resources.getString(R.string.correoInvalido)
            return false
        } else {
            tilEmailRegister.error = null
        }

        return true
    }

    private fun esPasswordValido(password: String): Boolean{
        if(password.length < 6){
            tilPasswordRegister.error = resources.getString(R.string.passwordInvalido)
            return false
        }
        return true
    }


}
