package com.example.parkingclientapplication.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.ConfirmSelectionFragment
import com.example.parkingclientapplication.fragments.VehicleSelectionFragment
import com.example.parkingclientapplication.inTransSelection
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.app_bar_reservation.*

class ReservationActivity : AppCompatActivity(), LoadFragments {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)


        supportFragmentManager.inTransSelection {
            replace(R.id.content_client_reservation, VehicleSelectionFragment())
        }
        setSupportActionBar(ReservationToolbar)
        ReservationToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        ReservationToolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 1){

                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)
            }else{
                supportFragmentManager.popBackStack()
            }
            Toast.makeText(this, supportFragmentManager.backStackEntryCount.toString(), Toast.LENGTH_SHORT).show()

        }


    }

    override fun loadFragment(fragment: Int) {
        when (fragment) {
            1 -> supportFragmentManager.inTransSelection {
                replace(R.id.content_client_reservation, ConfirmSelectionFragment())
            }
            2 -> {
                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)}
            }
        }
    }
