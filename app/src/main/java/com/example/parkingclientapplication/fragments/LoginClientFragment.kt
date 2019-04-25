package com.example.parkingclientapplication.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.activities.ClientMapActivity
import com.example.parkingclientapplication.interfaces.LoadFragments

class LoginClientFragment : Fragment() {


    private lateinit var loadFragments: LoadFragments

    private lateinit var buttonAccess: Button
    private lateinit var buttonRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        buttonAccess = view.findViewById(R.id.buttonAccess)
        buttonRegister = view.findViewById(R.id.buttonRegister)

        loadFragments = activity as LoadFragments

        buttonAccess.setOnClickListener {
            val intent = Intent(activity, ClientMapActivity::class.java)
            startActivity(intent)
        }

        buttonRegister.setOnClickListener {
            val bundle = Bundle()
            loadFragments.loadFragment(1, bundle)
        }
        // Inflate the layout for this fragment
        return view
    }




}
