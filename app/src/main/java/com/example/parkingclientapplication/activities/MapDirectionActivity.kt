package com.example.parkingclientapplication.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.MapDirectionFragment
import com.example.parkingclientapplication.inTransaction
import kotlinx.android.synthetic.main.activity_client_menu.*

class MapDirectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_direction)

        supportFragmentManager.inTransaction {
            add(R.id.form_clientNavMenu, MapDirectionFragment())
        }

        //Logout action on custom toolbar layout
        logout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
