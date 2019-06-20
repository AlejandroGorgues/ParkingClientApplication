package com.example.parkingclientapplication

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.parkingclientapplication.activities.GuideActivity
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.example.parkingclientapplication.model.Reservation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionIntentService : IntentService("intentS") {

    private val TAG = "GeofenceTransitionsIS"

    private val CHANNEL_ID = "channel_01"
    private lateinit var reservation: Reservation

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)


        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences



            // Send notification and log the transition details.
            loadGuideActivity(intent!!.getBundleExtra("reservationSelected")!!.getParcelable("reservation")!!)

        } else {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition))
        }
    }

    /**
     * Load the guide activity.
     */
    private fun loadGuideActivity(reservation: Reservation) {
        val bundle = Bundle()
        val intent = Intent(this, GuideActivity::class.java)
        bundle.putParcelable("reservation", reservation)
        intent.putExtra("reservationSelected", bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}

