package com.example.parkingclientapplication.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.example.parkingclientapplication.R
import android.widget.ImageView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.BarcodeFormat
import android.graphics.Color
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Button
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.*


class QrClientFragment : Fragment() {
    private var outputImage: ImageView? = null

    private lateinit var scanner_btn: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_client, container, false)
        outputImage =view!!.findViewById(R.id.imageView)
        scanner_btn = view.findViewById(R.id.scanner_btn)

        scanner_btn.setOnClickListener {
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


