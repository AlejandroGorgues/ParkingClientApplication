package com.example.parkingclientapplication.fragments


import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.Toast

import com.example.parkingclientapplication.R
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class SearchParkingLotFragment : Fragment() {

    private var mPreviewSize:Size? = null
    private var mTextureView:TextureView? = null
    private var mCameraDevice:CameraDevice? = null
    private var mPreviewBuilder:CaptureRequest.Builder? = null
    private var mPreviewSession:CameraCaptureSession? = null
    private var mBtnShot:Button? = null

    private var jpegSizes: Array<Size>? = null

    private val orientations = SparseIntArray()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_search_parking_lot, container, false)
        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)
        activity!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mTextureView = view.findViewById(R.id.texture) as TextureView
        mTextureView!!.surfaceTextureListener = mSurfaceTextureListener
        mBtnShot = view.findViewById(R.id.btn_takepicture) as Button
        mBtnShot!!.setOnClickListener {
            Log.e(TAG, "mBtnShot clicked")
            takePicture()
        }

        return view
    }


    private fun takePicture()
    {
        Log.e(TAG, "takePicture")

        if (null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return")
            return
        }
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics: CameraCharacteristics? = manager.getCameraCharacteristics(mCameraDevice!!.id)

            if (characteristics != null) {
                jpegSizes = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes != null && jpegSizes!!.isNotEmpty()) {
                width = jpegSizes!![0].width
                height = jpegSizes!![0].height
            }

            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(mTextureView!!.surfaceTexture))

            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            // Orientation
            val rotation = activity!!.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation))

            val file = File(Environment.getExternalStorageDirectory().toString() + "/DCIM", "pic.jpg")

            val readerListener = object:ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader:ImageReader) {
                    var image: Image? = null
                    try
                    {
                        image = reader.acquireLatestImage()
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        save(bytes)
                    }
                    catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                    finally
                    {
                        image?.close()
                    }
                }
                @Throws(IOException::class)
                private fun save(bytes:ByteArray) {
                    var output: OutputStream? = null
                    try
                    {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    }
                    finally
                    {
                        output?.close()
                    }
                }
            }

            val thread = HandlerThread("CameraPicture")
            thread.start()
            val backgroudHandler = Handler(thread.looper)
            reader.setOnImageAvailableListener(readerListener, backgroudHandler)
            val captureListener = object:CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session:CameraCaptureSession,
                                                request:CaptureRequest, result:TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(context, "Saved:$file", Toast.LENGTH_SHORT).show()
                    startPreview()
                }
            }
            mCameraDevice!!.createCaptureSession(outputSurfaces, object:CameraCaptureSession.StateCallback() {
                override fun onConfigured(session:CameraCaptureSession) {
                    try
                    {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler)
                    }
                    catch (e:CameraAccessException) {
                        e.printStackTrace()
                    }
                }
                override fun onConfigureFailed(session:CameraCaptureSession) {
                }
            }, backgroudHandler)
        } catch (e:CameraAccessException){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != mCameraDevice) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
    }

    private fun openCamera() {
            val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            Log.e(TAG, "openCamera E")
            try {
                val cameraId = manager.cameraIdList[0]
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                mPreviewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return
                }
                manager.openCamera(cameraId, mStateCallback, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            Log.e(TAG, "openCamera X")
    }

    private val mSurfaceTextureListener = object:TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface:SurfaceTexture, width:Int, height:Int) {
            Log.e(TAG, "onSurfaceTextureAvailable, width=$width,height=$height")
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface:SurfaceTexture,
                                                 width:Int, height:Int) {
            Log.e(TAG, "onSurfaceTextureSizeChanged")
        }
        override fun onSurfaceTextureDestroyed(surface:SurfaceTexture):Boolean {
            return false
        }
        override fun onSurfaceTextureUpdated(surface:SurfaceTexture) {
            Log.e(TAG, "onSurfaceTextureUpdated")
        }
    }

    private val mStateCallback = object:CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            Log.e(TAG, "onOpened")
            mCameraDevice = camera
            startPreview()
        }

        override fun onDisconnected(p0: CameraDevice) {
            Log.e(TAG, "onDisconnected")
        }

        override fun onError(p0: CameraDevice, p1: Int) {
            Log.e(TAG, "onError")
        }

    }

    private fun startPreview(){
        if(null == mCameraDevice || !mTextureView!!.isAvailable || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return")
            return
        }

        val  texture = mTextureView!!.surfaceTexture
        if(null == texture) {
            Log.e(TAG,"texture is null, return")
            return
        }

        texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        val surface = Surface(texture)
        try {
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch ( e:CameraAccessException) {

            e.printStackTrace()
        }
        mPreviewBuilder!!.addTarget(surface)

        try {
            mCameraDevice!!.createCaptureSession(Arrays.asList(surface), object:CameraCaptureSession.StateCallback() {
                override fun onConfigured(session:CameraCaptureSession) {
                    mPreviewSession = session
                    updatePreview()
                }
                override fun onConfigureFailed(session:CameraCaptureSession) {
                    Toast.makeText(context, "onConfigureFailed", Toast.LENGTH_LONG).show()
                }
            }, null)
        } catch ( e: CameraAccessException) {

            e.printStackTrace()
        }
    }

    private fun updatePreview(){
        if (null == mCameraDevice)
        {
            Log.e(TAG, "updatePreview error, return")
        }
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        val thread = HandlerThread("CameraPreview")
        thread.start()
        val backgroundHandler = Handler(thread.looper)
        try
        {
            mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), null, backgroundHandler)
        }
        catch (e:CameraAccessException) {
            e.printStackTrace()
        }
    }
}
