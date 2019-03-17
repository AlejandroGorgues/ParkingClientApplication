package com.example.parkingclientapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.parkingclientapplication.fragments.ExpensesClientFragment
import com.example.parkingclientapplication.fragments.ParkingListFragment
import com.example.parkingclientapplication.fragments.VehicleListFragment

class PagerClientMenuAdapter(fm: FragmentManager, private var tabCount: Int) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {

        return when (position) {
            0 -> VehicleListFragment()
            1 -> ParkingListFragment()
            2 -> ExpensesClientFragment()
            else -> null
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}