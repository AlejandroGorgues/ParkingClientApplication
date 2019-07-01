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
import android.support.v7.app.AppCompatActivity
import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.interfaces.LoadFragments
import kotlinx.android.synthetic.main.app_bar_guide.*
import java.util.*
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.*
import com.example.parkingclientapplication.AzureClient
import com.example.parkingclientapplication.model.ParkingLot
import com.example.parkingclientapplication.model.Reservation
import com.microsoft.windowsazure.mobileservices.MobileServiceClient
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable
import kotlinx.android.synthetic.main.content_client_guide.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException
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

    private lateinit var txtInfo: TextView

    private val TAG = GuideActivity::class.java.name

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private lateinit var paintLotsNS: Paint
    private lateinit var paintLotsS: Paint
    private lateinit var paintPath: Paint
    private lateinit var paintArrow: Paint
    private lateinit var paintText: Paint
    private lateinit var canvas: Canvas
    private lateinit var initialLotS: String
    private lateinit var finalLotS: String
    private lateinit var bitmap: Bitmap

    private lateinit var prbGuide: ProgressBar
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

        prbGuide = findViewById(R.id.guideProgressBar)
        txtInfo = findViewById(R.id.txtGuideInfo)

        devicesResult = ArrayList()
        parkingLots = ArrayList()
        reservationCheck = Reservation()

        reservation = intent!!.getBundleExtra("reservationSelected")!!.getParcelable("reservation")!!

        paintLotsNS = Paint()
        paintLotsS = Paint()
        paintPath = Paint()
        paintArrow = Paint()
        paintText = Paint()
        //initialLotS = "1-2"
        //finalLotS = "4-1"
        cornersRadius = 25
        txtInfo.text = resources.getString(R.string.plazaAdjudicada)



        /*
        * Check if the device have the SDK and permissions to activate and use Bluetooth LE service
         */
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


        /*
        * After checking it, waits to obtain the parking lot and proceed to obtain the route to be draw
         */
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
                //While there is no parking lot assigned, check it again
                while (reservationCheck.idParkingLot == null || reservationCheck.idParkingLot == ""){
                    val resultQuery = reservationTable!!.where().field("id").eq(reservation.id).execute().get()
                    for (reservationAux in resultQuery) {

                        reservationCheck = reservationAux

                    }
                }
                val resultParkingLotQuery =
                  parkingLotTable!!.where().field("id").eq(reservationCheck.idParkingLot).execute().get()

                for(parkingLot in resultParkingLotQuery){
                    finalLotS = parkingLot.position!!

                    uiThread {
                        txtInfo.text = resources.getString(R.string.rutaCalculada)
                        prbGuide.visibility = View.VISIBLE
                    }

                    //Start drawing related to the bluetooth devices near as well as the guiding node the route
                    obtainParkingLot()
                }


                }
        } catch (e: MalformedURLException) {
            AzureClient.getInstance(this).createAndShowDialog(Exception("There was an error creating the Mobile Service. Verify the URL"), resources.getString(R.string.error))
        } catch (e: java.lang.Exception){
            AzureClient.getInstance(this).createAndShowDialog(e, resources.getString(R.string.error))
        }

        setSupportActionBar(Guidetoolbar)
        Guidetoolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        Guidetoolbar.setNavigationOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 1){
                val intent = Intent(this, GuideMapActivity::class.java)
                startActivity(intent)
            }else{
                supportFragmentManager.popBackStack()
            }
            //Toast.makeText(this, supportFragmentManager.backStackEntryCount.toString(), Toast.LENGTH_SHORT).show()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun loadFragment(fragment: Int, bundle: Bundle) {
        when (fragment) {
            1 -> {
                val intent = Intent(this, ClientMapActivity::class.java)
                startActivity(intent)}
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
            Log.i(TAG, callbackType.toString())
            Log.i(TAG, result.toString())
            devicesResult!!.add(result)


        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i(TAG, sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Error Code: $errorCode")
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


    /*
    * Because the app know the address of the guiding node, we can connect to the device once we finish the scan
    * and obtained the closest bluetooth device in order to obtain the route
     */
    private fun selectDevice(){
        var highestRSSI = -100
        for (result in devicesResult!!){

          if(result.rssi >= highestRSSI && result.device.name != null){
              highestRSSI = result.rssi
              closestDeviceName = result.device.name
          }
        }
        val btDevice = devicesResult!!.filter { it.device.address ==  "B8:27:EB:D2:A6:DE"}
        //Log.e("DeviceClosest", closestDeviceName)
        initialLotS = closestDeviceName
        //Log.e("Device", btDevice.toString())
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
            Log.i(TAG, "Status: $status")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "STATE_CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e(TAG, "STATE_DISCONNECTED")
                }
                else -> Log.e(TAG, "STATE_OTHER")

            }
        }
        override fun onServicesDiscovered(gatt:BluetoothGatt, status:Int) {
            val services = gatt.services

            Log.i(TAG, services.toString())

            val characteristicList = services[2].characteristics


            /*
            * The guiding only have three characteristics
            * initialLot and finalLot are writeable
            * direction is readable
             */
            initialLot = characteristicList[0]
            finalLot = characteristicList[1]
            direction = characteristicList[2]

            operationCharacteristic(initialLot!!, gatt)
        }



        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCharacteristicRead(gatt:BluetoothGatt,
                                          characteristic:BluetoothGattCharacteristic, status:Int) {
            Log.i(TAG, characteristic.toString())
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(characteristic)
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

    /*
    * Because operations with characteristics are asynchronous
    * We send the lots one after another and then wait to read
     */

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
                gatt.writeCharacteristic(characteristic)

            } else if (characteristic.uuid == UUID.fromString("69d9fdd7-44fa-4987-aa3f-43b5f4cabcbf")) {
                descriptorSelected = "2"

                val value = finalLotS.toByteArray()
                //val value = "4-4".toByteArray()
                characteristic.value = value
                gatt.writeCharacteristic(characteristic)

            }
        }
    }

    /*
    * Because there are a Progress bar as well as a textView in the layout we created the canvas manually with a bitmap
    *
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic) {

        when (characteristic.uuid) {
            UUID.fromString("69d9fdd7-54fa-4987-aa3f-43b5f4cabcbf") -> {

                val screenSize = getScreenSizePixels()
                runOnUiThread {

                    //We created the bitmap with the screen size
                    bitmap = Bitmap.createBitmap(
                        screenSize[0], // Width
                        screenSize[1], // Height
                        Bitmap.Config.ARGB_8888 // Config
                    )

                    // Initialize a new Canvas instance
                    canvas = Canvas(bitmap)
                    //Associate the parking figures to the canvas
                    loadParking()

                    //Associate the route with lines that the driver must follow to the canvas
                    calculateStart(String(characteristic.value))

                    //Change the colour of the lot that the driver must go
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
                    prbGuide.visibility = View.GONE
                    txtInfo.visibility = View.GONE

                    //Draw the canvas into the bitmap
                    testing.setImageBitmap(bitmap)
                }
            }
        }
    }

    //Draw all the parking lots on the canvas
    private fun loadParking(){



        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE)

        // Initialize each paint that is going to be used to draw

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



        //Initialice the 16 rectangles that are going to be used

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

    }

    /*
    * Obtain the screen size in pixels
     */
    private fun getScreenSizePixels(): IntArray {
        val resources = resources
        val config = resources.configuration
        val dm = resources.displayMetrics
        val widthHeightInPixels = IntArray(2)
        // Note, screenHeightDp isn't reliable
        // (it seems to be too small by the height of the status bar),
        // but we assume screenWidthDp is reliable.
        // Note also, dm.widthPixels,dm.heightPixels aren't reliably pixels
        // (they get confused when in screen compatibility mode, it seems),
        // but we assume their ratio is correct.
        val screenWidthInPixels = config.screenWidthDp.toDouble() * dm.density
        val screenHeightInPixels = screenWidthInPixels * dm.heightPixels / dm.widthPixels
        widthHeightInPixels[0] = (screenWidthInPixels + .5).toInt()
        widthHeightInPixels[1] = (screenHeightInPixels + .5).toInt()
        return widthHeightInPixels
    }

    /*
    * Draw the arrow to orientate the driver where must go
     */
    private fun fillArrow(x0: Float, y0: Float, x1: Float, y1: Float, arrowHeadAngle: Int) {


        val arrowHeadLenght = 25
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

    /*
    * Calculate the orientation and position where the lines must go
     */
    private fun calculateStart(letters: String){

        var x1 = 0f
        var y1 = 0f
        var orientation = ""

        //Divide the initial and final lot to draw the lines
        val firstNumber = initialLotS[0].toString().toInt()
        val secondNumber = initialLotS[2].toString().toInt()
        val firstNumberF = finalLotS[0].toString().toInt()
        val secondNumberF = finalLotS[2].toString().toInt()

        /*
        * Check if the dirver comes from west or east
         */
        if(firstNumber == 1 || firstNumber == 4){
            //Si empieza por el oeste sino, empieza por el este
            if(firstNumber == 1){
                orientation = "W"
                if(secondNumberF > 2){
                    x1 = 25f
                    y1 = 800f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1, y1 + 50, paintText)
                }else{
                    x1 = 25f
                    y1 = 1200f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1, y1 - 50, paintText)
                }

            }else if(firstNumber == 4){
                orientation = "E"
                if(secondNumberF > 2){
                    x1 = 1025f
                    y1 = 800f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 200, y1 + 50, paintText)
                }else{
                    x1 = 1025f
                    y1 = 1200f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 200, y1 - 50, paintText)
                }

            }

            /*
            * Check if the driver comes from north or south
             */
        }else if(secondNumber == 1 || secondNumber == 4){
            //Si empieza por el sur, sino, empieza por el norte
            if (secondNumber == 1){
                orientation = "S"
                if(firstNumberF > 2){
                    x1 = 600f
                    y1 = 1550f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 150, y1 + 100, paintText)
                }else{
                    x1 = 450f
                    y1 = 1550f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 150, y1 + 100, paintText)
                }

            }else if (secondNumber == 4){
                orientation = "N"
                if(firstNumberF > 2){
                    x1 = 600f
                    y1 = 475f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 150, y1 - 100, paintText)
                }else{
                    x1 = 450f
                    y1 = 475f
                    canvas.drawText(resources.getString(R.string.lugarInicio), x1 - 150, y1 - 100, paintText)
                }
            }

        }

        drawLine(x1, y1, orientation, letters)

    }

    /*
    * Draw all the lines from the start to the end
     */
    private fun drawLine(x: Float, y: Float, orientation: String, letters: String){
        val offsetH = 400f //100 en 100
        val offsetV = 325f //50 en 50
        val offsetHE = 200f
        val offsetVE = 450f

        var x1 = x
        var y1 = y

        var i1 = initialLotS[0].toString().toInt()
        var i2 = initialLotS[2].toString().toInt()

        var letter: String
        for( i in 0 until letters.length){

            letter = letters[i].toString()
            /*
             * Check if the final lot is not at the start to avoid drawing lines and arrows
             * If not, it will check the orientation of the line (turn left, right with "D" or goes stratight with "R")
             * Then it checks if the road pass between two parking lots to apply more distance (offsetHE for horizontal gap or offsetVE for vertical gap) with the arrow
             * If not it only applies normal gap with the arrow
              */
            if(letter != "P"){
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
                                    fillArrow(x, y1 +  offsetVE, x1, y1, 225)
                                    offsetVE
                                }else{
                                    canvas.drawLine(x1, y1, x1, y1 + offsetV, paintPath)
                                    fillArrow(x1, y1 +  offsetVE, x1, y1, 225)
                                    offsetV
                                }
                                i2 -=1
                            }
                            "D" -> {
                                x1 -= if(limit(i1, i2,  i1 -1, i2, orientation)) {
                                    canvas.drawLine(x1, y1, x1 - offsetHE, y1, paintPath)
                                    fillArrow(x1 - offsetHE, y1, x1, y1, 225)
                                    offsetHE
                                } else {
                                    canvas.drawLine(x1, y1, x1 - offsetH, y1, paintPath)
                                    fillArrow(x1 - offsetHE, y1, x1, y1, 225)
                                    offsetH
                                }
                                i1 -=1
                            }
                            else -> {
                                x1 += if(limit(i1, i2, i1 +1  , i2, orientation)) {
                                    canvas.drawLine(x1, y1, x1 + offsetHE, y1, paintPath)
                                    fillArrow(x1 + offsetHE, y1, x1, y1, 225)
                                    offsetHE
                                } else {
                                    canvas.drawLine(x1, y1, x1 + offsetH, y1, paintPath)
                                    fillArrow(x1 + offsetHE, y1, x1, y1, 225)
                                    offsetH
                                }
                                i1 +=1
                            }
                        }
                    }
                }
            }
        }


    }

    /*
    * Check if between two parking lots there is a road in order to draw another line lenght
     */
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