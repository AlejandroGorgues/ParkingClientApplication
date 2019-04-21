package com.example.parkingclientapplication.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.*
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.activity_client_map.*
import kotlinx.android.synthetic.main.app_bar_client_map.*
import com.example.parkingclientapplication.interfaces.GetCurrentActivity
import com.google.zxing.integration.android.IntentIntegrator


class ClientMapActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LoadFragments,GetCurrentActivity, ActivityCompat.OnRequestPermissionsResultCallback  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_map)

        supportFragmentManager.inTransaction {
            replace(R.id.content_data_client, MapParkingFragment())
        }


        setSupportActionBar(Maptoolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, Maptoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_profile -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ProfileClientFragment(), "perfil")
                }
            }
            R.id.nav_reservations -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ReservationListClientFragment(), "reserv")
                }
            }
            R.id.nav_vehicles -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, VehicleListFragment(), "vehiculo")
                }
            }
            R.id.nav_expenses -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ExpensesClientFragment(), "gastos")
                }
            }
            R.id.nav_map -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, MapParkingFragment(), "Mapa")
                }
            }
            R.id.nav_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_information -> {

                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, QrClientFragment(), "qr")
                }
            }
            R.id.nav_license_plate -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, LicensePlateRecognition(), "license")
                }
            }
            R.id.nav_guide -> {

            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun loadFragment(fragment: Int) {
        when (fragment) {
            1 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, MapParkingFragment())
            }
            2 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, VehicleModifiedFragment())
            }
            3 ->{
                val intent = Intent(this, ReservationActivity::class.java)
                startActivity(intent)}
            4 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, VehicleAddFragment())
            }
            5 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, ConfirmSelectionFragment())
            }
        }
    }

    override fun getCurrentActivity(): AppCompatActivity {
        return this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.e("aqui", "Cancelled scan")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.e("aqui", "Scanned")
                Toast.makeText(
                    this,
                    "FORMAT: " + result.formatName + " \nCONTENT: " + result.contents,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
