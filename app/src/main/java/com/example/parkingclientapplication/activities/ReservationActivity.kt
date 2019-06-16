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
import com.example.parkingclientapplication.model.Parking
import kotlinx.android.synthetic.main.app_bar_reservation.*

class ReservationActivity : AppCompatActivity(), LoadFragments {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)


        val vehicleSelecionFragment = VehicleSelectionFragment()
        vehicleSelecionFragment.arguments = intent.getBundleExtra("parkingSelected")
        supportFragmentManager.inTransSelection {
            replace(R.id.content_client_reservation, vehicleSelecionFragment)
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

        }


    }

    override fun loadFragment(fragment: Int, bundle:Bundle) {
        when (fragment) {
            1 -> {
                val confirmSelectionFragment = ConfirmSelectionFragment()
                confirmSelectionFragment.arguments = bundle
                supportFragmentManager.inTransSelection {
                replace(R.id.content_client_reservation, confirmSelectionFragment)}
            }
            2 -> {
                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)}

            3 -> {
                val intent = Intent(this, GuideActivity::class.java)
                intent.putExtra("parkingLotSelected", bundle)
                startActivity(intent)}
            }
        }
    }
