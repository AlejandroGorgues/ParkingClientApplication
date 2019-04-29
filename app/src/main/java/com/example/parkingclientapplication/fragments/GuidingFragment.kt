package com.example.parkingclientapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import com.example.parkingclientapplication.R

class GuidingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_guiding, container, false)



        return view
    }
}
