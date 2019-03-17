package com.example.parkingclientapplication.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.activities.ClientMenuActivity


class RegisterClientFragment : Fragment() {

    private lateinit var buttonAccess: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register_client, container, false)

        buttonAccess = view!!.findViewById(R.id.buttonAccess)
        buttonAccess.setOnClickListener {
            val intent = Intent(activity, ClientMenuActivity::class.java)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view
    }


}
