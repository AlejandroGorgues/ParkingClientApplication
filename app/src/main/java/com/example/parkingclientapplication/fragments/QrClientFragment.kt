package com.example.parkingclientapplication.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.nineyards.anyline.camera.CameraOpenListener

import com.example.parkingclientapplication.R
import java.lang.Exception
import android.widget.TextView
import at.nineyards.anyline.camera.CameraController
import io.anyline.plugin.barcode.BarcodeScanViewPlugin
import io.anyline.view.ScanView
import io.anyline.view.BaseScanViewConfig
import io.anyline.view.ScanViewPluginConfig
import android.view.WindowManager




class QrClientFragment : Fragment(), CameraOpenListener {


    private var barcodeScanView: ScanView? = null
    private var resultText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_client, container, false)
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        resultText = view.findViewById(R.id.text_result) as TextView

        barcodeScanView = view.findViewById(R.id.scan_view) as ScanView
        // add a camera open listener that will be called when the camera is opened or an error occurred
        //  this is optional (if not set a RuntimeException will be thrown if an error occurs)
        barcodeScanView!!.setCameraOpenListener(this)
        // the view can be configured via a json file in the assets, and this config is set here
        // (alternatively it can be configured via xml, see the Energy Example for that)
        //barcodeScanView.setScanConfig("barcode_view_config.json");

        // optionally limit the barcode format to (multiple) specific types
        //barcodeScanView.setBarcodeFormats(BarcodeScanView.BarcodeFormat.QR_CODE,
        //        BarcodeScanView.BarcodeFormat.CODE_128);
        //init the scanViewPlugin configuration which hold the scan view ui configuration (cutoutConfig and ScanFeedbackConfig)
        val barcodeScanviewPluginConfig = ScanViewPluginConfig(activity!!.applicationContext, "barcode_view_config.json")
        //init the scan view
        val scanViewPlugin = BarcodeScanViewPlugin(
            activity!!.applicationContext,
            getString(R.string.anyline_license_key),
            barcodeScanviewPluginConfig,
            "BARCODE"
        )
        //init the base scanViewconfig which hold camera and flash configuration
        val barcodeBaseScanViewConfig = BaseScanViewConfig(activity!!.applicationContext, "barcode_view_config.json")
        //set the base scanViewConfig to the ScanView
        barcodeScanView!!.setScanViewConfig(barcodeBaseScanViewConfig)
        //set the scanViewPlugin to the ScanView
        barcodeScanView!!.scanViewPlugin = scanViewPlugin
        //add result listener
        scanViewPlugin.addScanResultListener { result -> resultText!!.text = result.result.toString() }
        // Inflate the layout for this fragment
        return view
    }

    override fun onResume() {
        super.onResume()
        resultText!!.text = ""
        //start the actual scanning
        barcodeScanView!!.start()
    }

    override fun onPause() {
        super.onPause()
        //stop the scanning
        barcodeScanView!!.stop()
        //release the camera (must be called in onPause, because there are situations where
        // it cannot be auto-detected that the camera should be released)
        barcodeScanView!!.releaseCameraInBackground()
    }


    override fun onCameraOpened(cameraController: CameraController, width: Int, height: Int) {
        //the camera is opened async and this is called when the opening is finished

    }

    override fun onCameraError(e: Exception) {
        //This is called if the camera could not be opened.
        // (e.g. If there is no camera or the permission is denied)
        // This is useful to present an alternative way to enter the required data if no camera exists.
        throw RuntimeException(e)
    }


}
