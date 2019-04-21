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

    // barcode data
    val BARCODE_DATA = "123456"
    //QR Code data
    val QR_CODE_DATA = "www.skholingua.com"

    // barcode image
    var bitmap: Bitmap? = null
    var outputImage: ImageView? = null

    private lateinit var qr_code_btn: Button
    private lateinit var barcode_btn: Button
    private lateinit var scanner_btn: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_client, container, false)
        outputImage =view!!.findViewById(R.id.imageView)
        qr_code_btn = view.findViewById(R.id.qr_code_btn)
        barcode_btn = view.findViewById(R.id.barcode_btn)
        scanner_btn = view.findViewById(R.id.scanner_btn)

        qr_code_btn.setOnClickListener {
            bitmap = encodeAsBitmap(QR_CODE_DATA, BarcodeFormat.QR_CODE, 512, 512)
            outputImage!!.setImageBitmap(bitmap)
        }

        barcode_btn.setOnClickListener {
            bitmap = encodeAsBitmap(BARCODE_DATA, BarcodeFormat.CODE_128, 600, 300)
            outputImage!!.setImageBitmap(bitmap)
        }

        scanner_btn.setOnClickListener {
            val integrator = IntentIntegrator(this.activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)   //To scan all types of Barcodes
            integrator.setPrompt("Scan")   //Set message as SCAN
            integrator.setCameraId(0)  //Default camera as back camera/main camera
            integrator.setBeepEnabled(false)   //Enable scan sound for success or failure
            integrator.initiateScan()
        }
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
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

    private fun encodeAsBitmap(contents:String?, format:BarcodeFormat, imgWidth:Int, imgHeight:Int): Bitmap? {
        if (contents == null)
        {
            return null
        }
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val writer = MultiFormatWriter()
        val result: BitMatrix
        try
        {
            result = writer.encode(contents, format, imgWidth, imgHeight, hints)
        }
        catch (iae:IllegalArgumentException) {
            // Unsupported format
            return null
        }
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height)
        {
            val offset = y * width
            for (x in 0 until width)
            {
                pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height,
            Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}


