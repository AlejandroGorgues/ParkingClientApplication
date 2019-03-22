package com.example.parkingclientapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.example.parkingclientapplication.fragments.ExpensesClientFragment
import com.example.parkingclientapplication.fragments.MapParkingFragment
import com.example.parkingclientapplication.fragments.VehicleListFragment

class PagerClientMenuAdapter(fm: FragmentManager, private var tabCount: Int) :
    FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {

        return when (position) {
            0 -> VehicleListFragment()
            1 -> MapParkingFragment()
            2 -> ExpensesClientFragment()
            else -> null
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}