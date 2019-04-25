package com.example.parkingclientapplication.interfaces

import android.os.Bundle

interface LoadFragments {
    //Function to load fragment based on option selected
    fun loadFragment(fragment:Int, bundle:Bundle)

}