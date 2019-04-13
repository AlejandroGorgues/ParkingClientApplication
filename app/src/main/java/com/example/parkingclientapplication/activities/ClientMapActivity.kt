package com.example.parkingclientapplication.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.*
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.activity_client_map.*
import kotlinx.android.synthetic.main.app_bar_client_map.*

class ClientMapActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LoadFragments {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_map)

        supportFragmentManager.inTransaction {
            replace(R.id.content_data_client, MapParkingFragment())
        }

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.client_map, menu)
        return true
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_profile -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ProfileClientFragment())
                }
            }
            R.id.nav_reservations -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ReservationListClientFragment())
                }
            }
            R.id.nav_vehicles -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, VehicleListFragment())
                }
            }
            R.id.nav_expenses -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, ExpensesClientFragment())
                }
            }
            R.id.nav_map -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, MapParkingFragment())
                }
            }
            R.id.nav_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_information -> {
                supportFragmentManager.inTransaction {
                    replace(R.id.content_data_client, QrClientFragment())
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
            3 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, VehicleListFragment())
            }
            4 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, VehicleAddFragment())
            }
            5 -> supportFragmentManager.inTransaction {
                replace(R.id.content_data_client, ConfirmSelectionFragment())
            }
            else -> supportFragmentManager.inTransaction {
            replace(R.id.content_data_client, SearchParkingLotFragment())
            }
        }
    }

}
