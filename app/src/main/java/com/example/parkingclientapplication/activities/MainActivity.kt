package com.example.parkingclientapplication.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.LoginClientFragment
import com.example.parkingclientapplication.fragments.RegisterClientFragment
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments

class MainActivity : AppCompatActivity(), LoadFragments {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.inTransaction {
            add(R.id.form_login, LoginClientFragment())
        }

    }

    override fun loadFragment(fragment: Int, bundle: Bundle) {
        when (fragment) {
            1 -> supportFragmentManager.inTransaction {
                replace(R.id.form_login, RegisterClientFragment())
            }
        }
    }
}
