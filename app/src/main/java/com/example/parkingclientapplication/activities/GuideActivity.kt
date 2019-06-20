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
import android.graphics.*
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.DisplayMetrics
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.model.Driver
import com.example.parkingclientapplication.model.Parking
import com.example.parkingclientapplication.model.ParkingLot
import com.example.parkingclientapplication.model.Reservation
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.android.synthetic.main.content_client_guide.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URLEncoder
import java.nio.ByteBuffer
import kotlin.collections.ArrayList


class GuideActivity : AppCompatActivity(), LoadFragments, ActivityCompat.OnRequestPermissionsResultCallback{

    private var mClient: MobileServiceClient? = null

    private var reservationTable: MobileServiceTable<Reservation>? = null
    private var parkingLotTable: MobileServiceTable<ParkingLot>? = null

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val REQUEST_ENABLE_BT = 1
    private var handler: Handler? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mGatt: BluetoothGatt? = null
    private var initialLot: BluetoothGattCharacteristic? = null
    private var finalLot: BluetoothGattCharacteristic? = null
    private var direction: BluetoothGattCharacteristic? = null

    private lateinit var reservation: Reservation
    private lateinit var reservationCheck: Reservation

    private var devicesResult: ArrayList<ScanResult>? = null
    private var parkingLots: ArrayList<ParkingLot>? = null

    private var closestDeviceName: String = ""

    private var descriptorSelected = ""

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    private lateinit var routeTxt: TextView

    private lateinit var paintLotsNS: Paint
    private lateinit var paintLotsS: Paint
    private lateinit var paintPath: Paint
    private lateinit var paintArrow: Paint
    private lateinit var paintText: Paint
    private lateinit var canvas: Canvas
    private lateinit var initialLotS: String
    private lateinit var finalLotS: String
    private var finalRectangle = ""
    private val parkingRectangles:HashMap<String, RectF> = HashMap()
    private var cornersRadius = 0


    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        handler = Handler()

        devicesResult = ArrayList()
        parkingLots = ArrayList()
        reservationCheck = Reservation()

        reservation = intent!!.getBundleExtra("reservationSelected")!!.getParcelable("reservation")!!

        paintLotsNS = Paint()
        paintLotsS = Paint()
        paintPath = Paint()
        paintArrow = Paint()
        paintText = Paint()
        initialLotS = "1-2"
        finalLotS = "4-1"
        cornersRadius = 25



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


        try {
            // Create the client instance, using the provided mobile app URL.
            mClient = AzureClient.getInstance(this).getClient()



            mClient!!.setAndroidHttpClientFactory {
                val client = OkHttpClient()
                client.readTimeoutMillis()
                client.writeTimeoutMillis()
                client
            }

            reservationTable = mClient!!.getTable(Reservation::class.java)
            parkingLotTable = mClient!!.getTable(ParkingLot::class.java)
            doAsync {
                while (reservationCheck.idParkingLot == null || reservationCheck.idParkingLot == ""){
                    val resultQuery = reservationTable!!.where().field("id").eq(reservation.id).execute().get()
                    for (reservationAux in resultQuery) {

                        reservationCheck = reservationAux

                    }
                }
                val resultParkingLotQuery =
                  parkingLotTable!!.where().field("id").eq(reservationCheck.idParkingLot).execute().get()

                for(parkingLot in resultParkingLotQuery){
                    runOnUiThread {
                        loadParking()
                        calculateStart("RRRD")
                        finalRectangle = "rect" + finalLotS[0].toString().toInt() + ""+ finalLotS[2].toString().toInt()

                        if(parkingRectangles.containsKey(finalRectangle)){

                            val rectAux = RectF(
                                parkingRectangles[finalRectangle]!!.left , // left
                                parkingRectangles[finalRectangle]!!.top , // top
                                parkingRectangles[finalRectangle]!!.right , // right
                                parkingRectangles[finalRectangle]!!.bottom  // bottom
                            )

                            canvas.drawRoundRect(
                                rectAux, // rect
                                cornersRadius.toFloat(), // rx
                                cornersRadius.toFloat(), // ry
                                paintLotsS // Paint
                            )
                            parkingRectangles.replace(finalRectangle, rectAux)
                        }
                    }
                }


                }
        } catch (e: MalformedURLException) {
            AzureClient.getInstance(this).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), "Error")
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(this).createAndShowDialog(e, "Error")
        }
        //obtainParkingLot()


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
        /*if (bluetoothAdapter != null && !bluetoothAdapter!!.isEnabled)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

            mLEScanner = bluetoothAdapter!!.bluetoothLeScanner
            scanLeDevice(true)*/

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



    private fun obtainParkingLot(){

        startGuiding()
    }

    private fun startGuiding(){
        if (bluetoothAdapter != null && !bluetoothAdapter!!.isEnabled)
        {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }

        mLEScanner = bluetoothAdapter!!.bluetoothLeScanner
        scanLeDevice(true)
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

            }
        }
    }

    private fun loadParking(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val bitmap = Bitmap.createBitmap(
            displayMetrics.widthPixels, // Width
            displayMetrics.heightPixels, // Height
            Bitmap.Config.ARGB_8888 // Config
        )

        // Initialize a new Canvas instance
        canvas = Canvas(bitmap)


        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE)

        // Initialize a new Paint instance to draw the rounded rectangle

        paintLotsNS.style = Paint.Style.FILL
        paintLotsNS.color = Color.RED
        paintLotsNS.isAntiAlias = true

        paintLotsS.style = Paint.Style.FILL
        paintLotsS.color = Color.GREEN
        paintLotsS.isAntiAlias = true

        paintPath.style = Paint.Style.FILL
        paintPath.color = Color.BLUE
        paintPath.strokeWidth = 10f
        paintPath.isAntiAlias = true

        paintArrow.style = Paint.Style.STROKE
        paintArrow.color = Color.CYAN
        paintArrow.strokeWidth = 10f
        paintArrow.isAntiAlias = true

        paintText.style = Paint.Style.FILL
        paintText.color = Color.BLACK
        paintText.strokeWidth = 3f
        paintText.textSize = 40f

        paintText.isAntiAlias = true



        // Initialize a new RectF instance
        val rect11 = RectF(
            50.toFloat(), // left
            1400.toFloat(), // top
            200.toFloat(), // right
            1500.toFloat() // bottom
        )

        val rect12 = RectF(
            50.toFloat(), // left
            1250.toFloat(), // top
            200.toFloat(), // right
            1350.toFloat() // bottom
        )

        val rect21 = RectF(
            250.toFloat(), // left
            1400.toFloat(), // top
            400.toFloat(), // right
            1500.toFloat() // bottom
        )


        val rect22 = RectF(
            250.toFloat(), // left
            1250.toFloat(), // top
            400.toFloat(), // right
            1350.toFloat() // bottom
        )






        // Initialize a new RectF instance
        val rect14 = RectF(
            50.toFloat(), // left
            500.toFloat(), // top
            200.toFloat(), // right
            600.toFloat() // bottom
        )

        val rect13 = RectF(
            50.toFloat(), // left
            650.toFloat(), // top
            200.toFloat(), // right
            750.toFloat() // bottom
        )

        val rect23 = RectF(
            250.toFloat(), // left
            650.toFloat(), // top
            400.toFloat(), // right
            750.toFloat() // bottom
        )

        val rect24 = RectF(
            250.toFloat(), // left
            500.toFloat(), // top
            400.toFloat(), // right
            600.toFloat() // bottom
        )









        // Initialize a new RectF instance
        val rect31 = RectF(
            650.toFloat(), // left
            1400.toFloat(), // top
            800.toFloat(), // right
            1500.toFloat() // bottom
        )

        val rect32 = RectF(
            650.toFloat(), // left
            1250.toFloat(), // top
            800.toFloat(), // right
            1350.toFloat() // bottom
        )

        val rect41 = RectF(
            850.toFloat(), // left
            1400.toFloat(), // top
            1000.toFloat(), // right
            1500.toFloat() // bottom
        )


        val rect42 = RectF(
            850.toFloat(), // left
            1250.toFloat(), // top
            1000.toFloat(), // right
            1350.toFloat() // bottom
        )






        // Initialize a new RectF instance
        val rect33 = RectF(
            650.toFloat(), // left
            650.toFloat(), // top
            800.toFloat(), // right
            750.toFloat() // bottom
        )

        val rect34 = RectF(
            650.toFloat(), // left
            500.toFloat(), // top
            800.toFloat(), // right
            600.toFloat() // bottom
        )

        val rect43 = RectF(
            850.toFloat(), // left
            650.toFloat(), // top
            1000.toFloat(), // right
            750.toFloat() // bottom
        )

        val rect44 = RectF(
            850.toFloat(), // left
            500.toFloat(), // top
            1000.toFloat(), // right
            600.toFloat() // bottom
        )


        parkingRectangles["rect11"] = rect11
        parkingRectangles["rect12"] = rect12
        parkingRectangles["rect13"] = rect13
        parkingRectangles["rect14"] = rect14
        parkingRectangles["rect21"] = rect21
        parkingRectangles["rect22"] = rect22
        parkingRectangles["rect23"] = rect23
        parkingRectangles["rect24"] = rect24
        parkingRectangles["rect31"] = rect31
        parkingRectangles["rect32"] = rect32
        parkingRectangles["rect33"] = rect33
        parkingRectangles["rect34"] = rect34
        parkingRectangles["rect41"] = rect41
        parkingRectangles["rect42"] = rect42
        parkingRectangles["rect43"] = rect43
        parkingRectangles["rect44"] = rect44

        // Define the corners radius of rounded rectangle


        // Finally, draw the rounded corners rectangle object on the canvas

        for(parkingRectangle in parkingRectangles.values){
            canvas.drawRoundRect(
                parkingRectangle, // rect
                cornersRadius.toFloat(), // rx
                cornersRadius.toFloat(), // ry
                paintLotsNS // Paint
            )
        }

        // Display the newly created bitmap on app interface
        testing.setImageBitmap(bitmap)
    }

    private fun fillArrow(x0: Float, y0: Float, x1: Float, y1: Float, arrowHeadAngle: Int) {


        val arrowHeadLenght = 50
        // arrowHeadAngle = 45
        val linePts = floatArrayOf(x1 - arrowHeadLenght, y1, x1, y1)
        val linePts2 = floatArrayOf(x1, y1, x1, y1 + arrowHeadLenght)
        val rotateMat = Matrix()

        //get the center of the line

        //set the angle
        val angle = Math.atan2((y1 - y0).toDouble(), (x1 - x0).toDouble()) * 180 / Math.PI + arrowHeadAngle

        //rotate the matrix around the center
        rotateMat.setRotate(angle.toFloat(), x1, y1)
        rotateMat.mapPoints(linePts)
        rotateMat.mapPoints(linePts2)

        canvas.drawLine(linePts[0], linePts[1], linePts[2], linePts[3], paintArrow)
        canvas.drawLine(linePts2[0], linePts2[1], linePts2[2], linePts2[3], paintArrow)
    }
    private fun calculateStart(letters: String){

        var x1 = 0f
        var y1 = 0f
        var orientation = ""
        //Empieza dibujar las lineas
        val firstNumber = initialLotS[0].toString().toInt()
        val secondNumber = initialLotS[2].toString().toInt()

        if(firstNumber == 1 || firstNumber == 4){
            //Si empieza por el oeste sino, empieza por el este
            if(firstNumber == 1){
                orientation = "W"
                x1 = 50f
                y1 = 1000f
                canvas.drawText("Se encuentra aqui", x1 - 50, y1 - 100, paintText)
            }else if(firstNumber == 4){
                orientation = "E"
                x1 = 1000f
                y1 = 1000f
                canvas.drawText("Se encuentra aqui", x1 +50, y1 - 100, paintText)
            }

        }else if(secondNumber == 1 || secondNumber == 4){
            //Si empieza por el sur, sino, empieza por el norte
            if (secondNumber == 1){
                orientation = "S"
                x1 = 500f
                y1 = 1500f
                canvas.drawText("Se encuentra aqui", x1 - 150, y1 + 100, paintText)
            }else if (secondNumber == 4){
                orientation = "N"
                x1 = 500f
                y1 = 400f
                canvas.drawText("Se encuentra aqui", x1 - 150, y1 - 100, paintText)
            }

        }

        drawLine(x1, y1, orientation, letters)

    }

    private fun drawLine(x: Float, y: Float, orientation: String, letters: String){
        val offsetH = 375f //100 en 100
        val offsetV = 300f //50 en 50
        val offsetHE = 250f
        val offsetVE = 650f

        var x1 = x
        var y1 = y

        var i1 = initialLotS[0].toString().toInt()
        var i2 = initialLotS[2].toString().toInt()

        var letter: String
        for( i in 0 until letters.length){

            letter = letters[i].toString()

            when (orientation) {
                "W" -> {
                    when (letter) {
                        "R" -> {

                            x1 += if(limit(i1, i2, i1 +1, i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 + offsetHE, y1, paintPath)
                                fillArrow(x1 + offsetHE, y1, x1, y1, 225)

                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 + offsetH, y1, paintPath)
                                fillArrow(x1 + offsetH, y1, x1, y1, 225)
                                offsetH
                            }
                            i1 += 1

                        }
                        "D" -> {
                            y1 += if(limit(i1, i2, i1, i2 - 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 + offsetVE, paintPath)
                                fillArrow(x1, y1 + offsetVE, x1, y1, 225)

                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 + offsetV, paintPath)
                                fillArrow(x1, y1 + offsetV, x1, y1, 225)
                                offsetV
                            }

                            i2 -= 1
                        }
                        else -> {
                            y1 -= if(limit(i1, i2, i1, i2 + 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 - offsetVE, paintPath)
                                fillArrow(x1, y1 - offsetVE, x1, y1, 225)

                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 - offsetV, paintPath)
                                fillArrow(x1, y1 - offsetV, x1, y1, 225)
                                offsetV
                            }
                            i2 +=1
                        }
                    }
                }
                "E" -> {
                    when (letter) {
                        "R" -> {

                            x1 -= if(limit(i1, i2,i1 -1, i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 - offsetHE, y1, paintPath)
                                fillArrow(x1 - offsetHE, y1, x1, y1, 225)

                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 - offsetH, y1, paintPath)
                                fillArrow(x1 - offsetH, y1, x1, y1, 225)
                                offsetH
                            }
                            i1 -=1
                        }
                        "D" -> {
                            y1 -= if(limit(i1, i2, i1, i2 + 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 - offsetVE, paintPath)
                                fillArrow(x1, y1 - offsetVE, x1, y1, 225)

                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 - offsetV, paintPath)
                                fillArrow(x1, y1 - offsetV, x1, y1, 225)
                                offsetV
                            }
                            i2 +=1
                        }
                        else -> {
                            y1 += if(limit(i1, i2, i1, i2 - 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 + offsetVE, paintPath)
                                fillArrow(x1, y1 + offsetVE, x1, y1, 225)

                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 + offsetV, paintPath)
                                fillArrow(x1, y1 + offsetV, x1, y1, 225)
                                offsetV
                            }
                            i2 -=1
                        }
                    }
                }
                "S" -> {
                    when (letter) {
                        "R" -> {
                            y1 -= if(limit(i1, i2, i1, i2 + 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 - offsetVE, paintPath)
                                fillArrow(x1, y1 - offsetVE, x1, y1, 225)

                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 - offsetV, paintPath)
                                fillArrow(x1, y1 - offsetV, x1, y1, 225)
                                offsetV
                            }
                            i2 +=1
                        }
                        "D" -> {
                            x1 += if(limit(i1, i2, i1 +1 , i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 + offsetHE, y1, paintPath)
                                fillArrow(x1 + offsetHE, y1, x1, y1, 225)

                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 + offsetH, y1, paintPath)
                                fillArrow(x1 + offsetH, y1, x1, y1, 225)
                                offsetH
                            }
                            i1 +=1
                        }
                        else -> {
                            x1 -= if(limit(i1, i2, i1 - 1, i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 - offsetHE, y1, paintPath)
                                fillArrow(x1 - offsetHE, y1, x1, y1, 225)

                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 - offsetH, y1, paintPath)
                                fillArrow(x1 - offsetH, y1, x1, y1, 225)
                                offsetH
                            }
                            i1 -=1
                        }
                    }
                }
                else -> {
                    when (letter) {
                        "R" -> {
                            y1 += if(limit(i1, i2, i1, i2 - 1, orientation)){
                                canvas.drawLine(x1, y1, x1, y1 + offsetVE, paintPath)
                                fillArrow(x1, y1 + offsetVE, x1, y1, 225)
                                offsetVE
                            }else{
                                canvas.drawLine(x1, y1, x1, y1 + offsetV, paintPath)
                                fillArrow(x1, y1 + offsetV, x1, y1, 225)
                                offsetV
                            }
                            i2 -=1
                        }
                        "D" -> {
                            x1 -= if(limit(i1, i2,  i1 -1, i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 - offsetHE, y1, paintPath)
                                fillArrow(x1 - offsetHE, y1 , x1, y1, 225)
                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 - offsetH, y1, paintPath)
                                fillArrow(x1 - offsetH, y1 , x1, y1, 225)
                                offsetH
                            }
                            i1 -=1
                        }
                        else -> {
                            x1 += if(limit(i1, i2, i1 +1  , i2, orientation)) {
                                canvas.drawLine(x1, y1, x1 + offsetHE, y1, paintPath)
                                fillArrow(x1 + offsetHE, y1 , x1, y1, 225)
                                offsetHE
                            } else {
                                canvas.drawLine(x1, y1, x1 + offsetH, y1, paintPath)
                                fillArrow(x1 + offsetH, y1 , x1, y1, 225)
                                offsetH
                            }
                            i1 +=1
                        }
                    }
                }
            }
        }

    }

    private fun limit(x1: Int, y1: Int, x2: Int, y2: Int, orientation: String):Boolean{
        when (orientation) {
            "W" -> {
                if(x1 == 2 && x2 == 3){
                    return true
                }
            }
            "E" -> {
                if(x1 == 3 && x2 == 2){
                    return true
                }
            }
            "S" -> {
                if(y1 == 2 && y2 == 3){
                    return true
                }
            }
            "N" -> {
                if(y1 == 3 && y2 == 2){
                    return true
                }
            }
        }
        return false
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





}