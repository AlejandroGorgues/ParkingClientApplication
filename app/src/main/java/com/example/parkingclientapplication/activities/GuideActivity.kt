package com.example.parkingclientapplication.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.app_bar_guide.*
import java.util.*
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import com.example.parkingclientapplication.model.ParkingLot
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.ByteBuffer
import kotlin.collections.ArrayList


class GuideActivity : AppCompatActivity(), LoadFragments, ActivityCompat.OnRequestPermissionsResultCallback{

    private var paint: Paint? = null

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val REQUEST_ENABLE_BT = 1
    private var handler: Handler? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null
    private var initialLot: BluetoothGattCharacteristic? = null
    private var finalLot: BluetoothGattCharacteristic? = null
    private var direction: BluetoothGattCharacteristic? = null

    private var parkingLot: ParkingLot? = null

    private var devicesResult: ArrayList<ScanResult>? = null

    private var closestDeviceName: String = ""

    private var descriptorSelected = ""

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private lateinit var linesView: ImageView
    private lateinit var routeTxt: TextView

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        handler = Handler()
        //linesView = findViewById(R.id.iv)
        routeTxt = findViewById(R.id.routeTxt)
        devicesResult = ArrayList()
        //parkingLot = intent.getBundleExtra("parkingLotSelected")!!.getParcelable("parkingLot")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION) }
                builder.show()
            }else{

                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                {
                    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        /*supportFragmentManager.inTransaction {
            replace(R.id.content_client_reservation, GuidingFragment())
        }*/
        setSupportActionBar(Guidetoolbar)
        Guidetoolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        Guidetoolbar.setNavigationOnClickListener {
            /*if (supportFragmentManager.backStackEntryCount == 1){
                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)
            }else{
                supportFragmentManager.popBackStack()
            }*/
            //Toast.makeText(this, supportFragmentManager.backStackEntryCount.toString(), Toast.LENGTH_SHORT).show()

        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter != null && !bluetoothAdapter!!.isEnabled)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

            mLEScanner = bluetoothAdapter!!.bluetoothLeScanner
            scanLeDevice(true)

    }
    override fun onPause() {
        super.onPause()
        if (bluetoothAdapter != null && bluetoothAdapter!!.isEnabled)
        {
            scanLeDevice(false)
        }
    }
    override fun onDestroy() {
        if (mGatt == null)
        {
            return
        }
        mGatt!!.close()
        mGatt = null
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.guide, menu)
        return true
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

    override fun loadFragment(fragment: Int, bundle: Bundle) {
        when (fragment) {
            1 -> {
                /*val confirmSelectionFragment = ConfirmSelectionFragment()
                confirmSelectionFragment.arguments = bundle
                supportFragmentManager.inTransSelection {
                    replace(R.id.content_client_reservation, confirmSelectionFragment)}*/
            }
            2 -> {
                /*val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)*/}
        }
    }

    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.i("callbackType", callbackType.toString())
            Log.i("result", result.toString())
            devicesResult!!.add(result)


        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i("ScanResult - Results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    private fun scanLeDevice(enable:Boolean) {
        if (enable)
        {
            handler!!.postDelayed({
                mLEScanner!!.stopScan(mScanCallback)

                selectDevice()
            }, SCAN_PERIOD)
            mLEScanner!!.startScan(mScanCallback)
        }
        else
        {
            mLEScanner!!.stopScan(mScanCallback)
        }
    }


    private fun selectDevice(){
        var highestRSSI = -100
        for (result in devicesResult!!){

          if(result.rssi >= highestRSSI && result.device.name != null){
              highestRSSI = result.rssi
              closestDeviceName = result.device.name
          }
        }
        val btDevice = devicesResult!!.filter { it.device.address ==  "B8:27:EB:D2:A6:DE"}
        Log.e("aqui", closestDeviceName)
        Log.e("aqui", btDevice.toString())
        connectToDevice(btDevice[0].device)
    }




    private fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback)
            scanLeDevice(false)// will stop after first device detection
        }
    }



    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt:BluetoothGatt, status:Int, newState:Int) {
            Log.i("onConnectionStateChange", "Status: $status")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("gattCallback", "STATE_CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e("gattCallback", "STATE_DISCONNECTED")
                }
                else -> Log.e("gattCallback", "STATE_OTHER")

            }
        }
        override fun onServicesDiscovered(gatt:BluetoothGatt, status:Int) {
            val services = gatt.services

            Log.i("onServicesDiscovered", services.toString())

            val characteristicList = services[2].characteristics


            initialLot = characteristicList[0]
            finalLot = characteristicList[1]
            direction = characteristicList[2]

            operationCharacteristic(initialLot!!, gatt)
        }



        override fun onCharacteristicRead(gatt:BluetoothGatt,
                                          characteristic:BluetoothGattCharacteristic, status:Int) {
            Log.i("onCharacteristicRead", characteristic.toString())
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate("sdfds", characteristic)
                }
            }

            gatt.disconnect()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if(characteristic!!.uuid == UUID.fromString("69d9fdd7-34fa-4987-aa3f-43b5f4cabcbf")){
                        operationCharacteristic(finalLot!!, gatt!!)
                    }else{
                        operationCharacteristic(direction!!, gatt!!)
                    }

                }
            }
        }
    }


    private fun operationCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        gatt: BluetoothGatt
    ) {
        if (isCharacteristicReadable(characteristic)) {
            gatt.readCharacteristic(characteristic)

        } else {

            if (characteristic.uuid == UUID.fromString("69d9fdd7-34fa-4987-aa3f-43b5f4cabcbf")) {
                descriptorSelected = "1"

                val value = closestDeviceName.toByteArray()
                characteristic.value = value
                val statusResult = gatt.writeCharacteristic(characteristic)
                Log.e("status1", statusResult.toString())
            } else if (characteristic.uuid == UUID.fromString("69d9fdd7-44fa-4987-aa3f-43b5f4cabcbf")) {
                descriptorSelected = "2"

                //val value = parkingLot!!.position!!.toByteArray()
                val value = "4-4".toByteArray()
                characteristic.value = value
                val statusResult = gatt.writeCharacteristic(characteristic)
                Log.e("status2", statusResult.toString())
            }
        }
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {

        when (characteristic.uuid) {
            UUID.fromString("69d9fdd7-54fa-4987-aa3f-43b5f4cabcbf") -> {
                Log.e("aqui", String(characteristic.value))
                routeTxt.text = String(characteristic.value)
                /*val  bitmap = Bitmap.createBitmap(
                        500, // Width
                        300, // Height
                        Bitmap.Config.ARGB_8888 // Config
                )

                // Initialize a new Canvas instance
                val canvas = Canvas(bitmap)

                // Draw a solid color on the canvas as background
                canvas.drawColor(Color.LTGRAY)

                // Initialize a new Paint instance to draw the line
                val paint = Paint()
                // Line color
                paint.color = Color.RED
                paint.style = Paint.Style.STROKE
                // Line width in pixels
                paint.strokeWidth = 8F
                paint.isAntiAlias = true

                // Set a pixels value to offset the line from canvas edge
                val offset = 50F

                /*
                    public void drawLine (float startX, float startY, float stopX, float stopY, Paint paint)
                        Draw a line segment with the specified start and stop x,y coordinates, using
                        the specified paint.

                        Note that since a line is always "framed", the Style is ignored in the paint.

                        Degenerate lines (length is 0) will not be drawn.

                    Parameters
                        startX : The x-coordinate of the start point of the line
                        startY : The y-coordinate of the start point of the line
                        paint : The paint used to draw the line

                */

                // Draw a line on canvas at the center position
                canvas.drawLine(
                        offset, // startX
                        canvas.height / 2F, // startY
                        canvas.width - offset, // stopX
                        canvas.height / 2F, // stopY
                        paint // Paint
                )

                // Display the newly created bitmap on app interface
                linesView.setImageBitmap(bitmap)*/
            }
        }
    }


    /**
     * @return Returns <b>true</b> if property is writable
     */
    fun isCharacteristicWriteable(pChar:BluetoothGattCharacteristic):Boolean {
        return (pChar.properties and(PROPERTY_WRITE or(PROPERTY_WRITE_NO_RESPONSE) )) != 0
    }
    /**
     * @return Returns <b>true</b> if property is Readable
     */
    private fun isCharacteristicReadable(pChar:BluetoothGattCharacteristic):Boolean {
        return ((pChar.properties and PROPERTY_READ) != 0)
    }
    /**
     * @return Returns <b>true</b> if property is supports notification
     */
    fun isCharacterisiticNotifiable(pChar:BluetoothGattCharacteristic):Boolean {
        return (pChar.properties and PROPERTY_NOTIFY) != 0
    }


    companion object {
        private const val SCAN_PERIOD: Long = 10000

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(FragmentActivity.TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }


}