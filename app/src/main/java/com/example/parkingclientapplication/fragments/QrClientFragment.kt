package com.example.parkingclientapplication.fragments

import android.os.Bundle

import com.example.parkingclientapplication.R
import android.widget.ImageView
import com.google.zxing.integration.android.IntentIntegrator
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Button


class QrClientFragment : Fragment() {
    private var outputImage: ImageView? = null

    private lateinit var scanneBtn: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_client, container, false)
        outputImage =view!!.findViewById(R.id.imageView)
        scanneBtn = view.findViewById(R.id.scanner_btn)

        //Scan the image in realtime
        scanneBtn.setOnClickListener {
            val integrator = IntentIntegrator(this.activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)   //To scan all types of Barcodes
            integrator.setPrompt("Scan")   //Set message as SCAN
            integrator.setCameraId(0)  //Default camera as back camera/main camera
            integrator.setBeepEnabled(false)   //Enable scan sound for success or failure
            integrator.initiateScan()
        }

        // Inflate the layout for this fragment
        return view
    }
}


