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
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.app_bar_guide.*
import java.util.*
import io.reactivex.internal.disposables.DisposableHelper.dispose
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import org.altbeacon.beacon.BeaconManager
import java.nio.ByteBuffer


class GuideActivity : AppCompatActivity(), LoadFragments, ActivityCompat.OnRequestPermissionsResultCallback{

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private var beaconList: ArrayList<String>? = null
    private var beaconListView: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var beaconManager: BeaconManager? = null
    private var handler: Handler? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null
    private var rxBleClient: RxBleClient? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        handler = Handler()
        beaconList = ArrayList()
        beaconListView = findViewById(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, beaconList)
        beaconListView!!.adapter = adapter
        rxBleClient = RxBleClient.create(this)
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

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
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
        else
        {
            mLEScanner = bluetoothAdapter!!.bluetoothLeScanner
            scanLeDevice(true)
            //mLEScanner = bluetoothAdapter!!.bluetoothLeScanner
            //scanLeDevice(true)
        }
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
            val btDevice = result.device
            if (btDevice.address == "B8:27:EB:D2:A6:DE"){
                connectToDevice(btDevice)
            }
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
            }, SCAN_PERIOD)
            mLEScanner!!.startScan(mScanCallback)
        }
        else
        {
            mLEScanner!!.stopScan(mScanCallback)
        }
    }



    fun connectToDevice(device: BluetoothDevice) {
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
                    //broadcastUpdate(intentAction)
                }
                else -> Log.e("gattCallback", "STATE_OTHER")

            }
        }
        override fun onServicesDiscovered(gatt:BluetoothGatt, status:Int) {
            val services = gatt.services

            Log.i("onServicesDiscovered", services.toString())
            gatt.readCharacteristic(services[2].characteristics[0])
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
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }


    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        //val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            UUID.fromString("69d9fdd7-34fa-4987-aa3f-43b5f4cabcbf") -> {
                /*Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 1).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 2).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 3).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 4).toString())*/
                val flag = characteristic.properties
                    Log.e("aqui", characteristic.getIntValue(FORMAT_SINT8, 1).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT16, 1).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT32, 1).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT8, 2).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT16, 2).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT32, 2).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT8, 3).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT16, 3).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT32, 3).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT8, 4).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT16, 4).toString())
                Log.e("aqui", characteristic.getIntValue(FORMAT_SINT32, 4).toString())

                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 1).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 2).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 3).toString())
                Log.e("aqui", characteristic.getFloatValue(FORMAT_FLOAT, 4).toString())

                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.e("aqui", characteristic.toString())
                Log.e("aqui", String.format("Received heart rate: %d", heartRate))
                //intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            UUID.fromString("69d9fdd7-44fa-4987-aa3f-43b5f4cabcbf") -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.e("aqui", characteristic.toString())
                Log.e("aqui", String.format("Received heart rate: %d", heartRate))
                //intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            UUID.fromString("69d9fdd7-54fa-4987-aa3f-43b5f4cabcbf") -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.e("aqui", characteristic.toString())
                Log.e("aqui", String.format("Received heart rate: %d", heartRate))
                //intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            UUID.fromString("69d9fdd7-64fa-4987-aa3f-43b5f4cabcbf") -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.e("aqui", characteristic.toString())
                Log.e("aqui", String.format("Received heart rate: %d", heartRate))
                //intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            UUID.fromString("69d9fdd7-74fa-4987-a3f4-3b5f4cabcbf") -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.e("aqui", characteristic.toString())
                Log.e("aqui", String.format("Received heart rate: %d", heartRate))
                //intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }

        }
        //sendBroadcast(intent)
    }

    /**
     * @return Returns <b>true</b> if property is writable
     */
    fun isCharacteristicWriteable(pChar:BluetoothGattCharacteristic):Boolean {
        return (pChar.properties and(BluetoothGattCharacteristic.PROPERTY_WRITE or(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) )) != 0
    }
    /**
     * @return Returns <b>true</b> if property is Readable
     */
    fun isCharacterisitcReadable(pChar:BluetoothGattCharacteristic):Boolean {
        return ((pChar.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0)
    }
    /**
     * @return Returns <b>true</b> if property is supports notification
     */
    fun isCharacterisiticNotifiable(pChar:BluetoothGattCharacteristic):Boolean {
        return (pChar.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
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
