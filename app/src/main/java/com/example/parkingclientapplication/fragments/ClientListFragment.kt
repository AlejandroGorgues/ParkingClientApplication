package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments

class ClientListFragment : Fragment() {

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_list, container, false)

        loadFragment = activity as LoadFragments

        // Inflate the layout for this fragment
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadFragment.configureTabLayout()
    }

}
