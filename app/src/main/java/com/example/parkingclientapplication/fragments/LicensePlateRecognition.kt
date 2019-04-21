package com.example.parkingclientapplication.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.example.parkingclientapplication.R
import io.anyline.view.ScanView
import io.anyline.view.BaseScanViewConfig
import io.anyline.plugin.licenseplate.LicensePlateScanViewPlugin
import io.anyline.view.ScanViewPluginConfig
import android.widget.Toast




class LicensePlateRecognition : Fragment() {

    private val TAG = LicensePlateRecognition::class.java.simpleName
    private var scanView: ScanView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val view = inflater.inflate(R.layout.fragment_license_plate_recognition, container, false)
        //Set the flag to keep the screen on (otherwise the screen may go dark during scanning)


        val license = getString(R.string.anyline_license_key)

        // Get the view from the layout
        scanView = view!!.findViewById(R.id.scan_view) as ScanView
        //init the scanViewPlugin configuration which hold the scan view ui configuration (cutoutConfig and ScanFeedbackConfig)
        val licensePlateScanviewPluginConfig =
            ScanViewPluginConfig(activity!!.applicationContext, "license_plate_view_config.json")
        //init the scan view
        val scanViewPlugin = LicensePlateScanViewPlugin(
            activity!!.applicationContext,
            getString(R.string.anyline_license_key),
            licensePlateScanviewPluginConfig,
            "LICENSE_PLATE"
        )
        //init the base scanViewconfig which hold camera and flash configuration
        val licensePlateBaseScanViewConfig =
            BaseScanViewConfig(activity!!.applicationContext, "license_plate_view_config.json")
        //set the base scanViewConfig to the ScanView
        scanView!!.setScanViewConfig(licensePlateBaseScanViewConfig)
        //set the scanViewPlugin to the ScanView
        scanView!!.scanViewPlugin = scanViewPlugin
        //add result listener
        scanViewPlugin.addScanResultListener { result ->
            //resultText.setText(result.result)
            Toast.makeText(context, result.result.toString(), Toast.LENGTH_LONG).show()
            startScanning()
        }
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater!!.inflate(R.menu.client_map, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun startScanning() {
        // this must be called in onResume, or after a result to start the scanning again
        if (!scanView!!.scanViewPlugin.isRunning) {
            scanView!!.start()
        }
    }

    override fun onResume() {
        super.onResume()
        startScanning()
    }

    override fun onPause() {
        super.onPause()

        scanView!!.stop()
        scanView!!.releaseCameraInBackground()
    }

}
