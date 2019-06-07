package com.example.parkingclientapplication.activities

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.fragments.GuidingFragment
import com.example.parkingclientapplication.inTransaction
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.app_bar_reservation.*
import android.util.Log


class GuideActivity : AppCompatActivity(), LoadFragments {

    private lateinit var handler: Handler
    private val mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null


    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        supportFragmentManager.inTransaction {
            replace(R.id.content_client_reservation, GuidingFragment())
        }
        setSupportActionBar(ReservationToolbar)
        ReservationToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        ReservationToolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 1){

                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)
            }else{
                supportFragmentManager.popBackStack()
            }
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
           scanLeDevice(true)
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

    private fun scanLeDevice(enable:Boolean) {
        if (enable)
        {
            handler.postDelayed({
                    mLEScanner!!.stopScan(mScanCallback)
            }, SCAN_PERIOD)
            mLEScanner!!.startScan(mScanCallback)
        }
        else
        {
            mLEScanner!!.stopScan(mScanCallback)
        }
    }

    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //Log.i("callbackType", callbackType.toString())
            //Log.i("result", result.toString())
            val btDevice = result.device
            connectToDevice(btDevice)
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
                }
                else -> Log.e("gattCallback", "STATE_OTHER")
            }
        }
        override fun onServicesDiscovered(gatt:BluetoothGatt, status:Int) {
            val services = gatt.services
            Log.i("onServicesDiscovered", services.toString())
            gatt.readCharacteristic(services[1].characteristics[0])
        }
        override fun onCharacteristicRead(gatt:BluetoothGatt,
                                          characteristic:BluetoothGattCharacteristic, status:Int) {
            Log.i("onCharacteristicRead", characteristic.toString())
            gatt.disconnect()
        }
    }

    companion object {
        private const val SCAN_PERIOD: Long = 10000
    }
}
