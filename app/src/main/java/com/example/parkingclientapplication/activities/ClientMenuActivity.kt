package com.example.parkingclientapplication.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.widget.TextView
import com.example.parkingclientapplication.PagerClientMenuAdapter
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.bind
import com.example.parkingclientapplication.fragments.ClientListFragment
import com.example.parkingclientapplication.fragments.VehicleAdd1TypeFragment
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.fragment_client_list.*

class ClientMenuActivity : AppCompatActivity(), LoadFragments {

    private val logout: TextView by bind<TextView>(R.id.logout)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_menu)

        supportFragmentManager.inTransaction {
            add(R.id.form_clientMenu, ClientListFragment())
        }

        logout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun loadFragment(fragment: Int) {
        when (fragment) {
            1 -> supportFragmentManager.inTransaction {
                replace(R.id.form_clientMenu, ClientListFragment())
            }
            2 -> supportFragmentManager.inTransaction {
                replace(R.id.form_clientMenu, ClientListFragment())
            }
            3 -> supportFragmentManager.inTransaction {
                replace(R.id.form_clientMenu, ClientListFragment())
            }
            5 -> supportFragmentManager.inTransaction {
                replace(R.id.form_clientMenu, VehicleAdd1TypeFragment())
            }
            else -> supportFragmentManager.inTransaction {
                replace(R.id.form_clientMenu, ClientListFragment())
            }
        }
    }

    override fun configureTabLayout() {
        tabLayout_clientList.addTab(tabLayout_clientList.newTab().setText("Tab 1 Item"))
        tabLayout_clientList.addTab(tabLayout_clientList.newTab().setText("Tab 2 Item"))
        tabLayout_clientList.addTab(tabLayout_clientList.newTab().setText("Tab 3 Item"))

        val adapter = PagerClientMenuAdapter(supportFragmentManager,
            tabLayout_clientList.tabCount)
        view_pager_clientList.adapter = adapter

        view_pager_clientList.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabLayout_clientList))
        tabLayout_clientList.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager_clientList.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
    }
}
