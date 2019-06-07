package com.example.parkingclientapplication.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.LoginClientFragment
import com.example.parkingclientapplication.fragments.RegisterClientFragment
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), LoadFragments {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.inTransaction {
            add(R.id.content_client_access, LoginClientFragment())
        }
        setSupportActionBar(MainAccessToolbar)


    }

    override fun loadFragment(fragment: Int, bundle: Bundle) {
        when (fragment) {
            1 -> supportFragmentManager.inTransaction {
                replace(R.id.content_client_access, RegisterClientFragment())
            }
            2 -> supportFragmentManager.inTransaction {
                replace(R.id.content_client_access, LoginClientFragment())
            }
        }
    }
}
