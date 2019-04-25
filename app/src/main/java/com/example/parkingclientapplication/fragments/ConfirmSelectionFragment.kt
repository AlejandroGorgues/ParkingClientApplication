package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments

class ConfirmSelectionFragment : Fragment() {
    private lateinit var buttonConfirm: Button

    private lateinit var loadFragment: LoadFragments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirm_selection, container, false)
        val bundle = Bundle()
        loadFragment = activity as LoadFragments
        buttonConfirm = view.findViewById(R.id.buttonConfirmation)

        buttonConfirm.setOnClickListener {
            loadFragment.loadFragment(1, bundle)
        }
        // Inflate the layout for this fragment
        return view
    }
}
