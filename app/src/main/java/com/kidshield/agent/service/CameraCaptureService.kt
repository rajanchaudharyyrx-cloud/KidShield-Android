package com.kidshield.agent.service

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraCaptureService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun capturePhoto(cameraId: String, callback: (ByteArray?) -> Unit) {
        startBackgroundThread()
        try {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCaptureSession(cameraId, callback)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    callback(null)
                }
            }, backgroundHandler)
        } catch (e: SecurityException) {
            Log.e("Camera", "Permission denied: ${e.message}")
            callback(null)
        }
    }

    private fun createCaptureSession(cameraId: String, callback: (ByteArray?) -> Unit) {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val size = map?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }
            ?: android.util.Size(1920, 1080)

        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 1)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            val buffer = image?.planes?.get(0)?.buffer
            val bytes = buffer?.let { ByteArray(it.remaining()).apply { buffer.get(this) } }
            image?.close()
            callback(bytes)
            closeCamera()
        }, backgroundHandler)

        val surfaces = mutableListOf<Surface>()
        imageReader?.surface?.let { surfaces.add(it) }

        cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                val captureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                    imageReader?.surface?.let { addTarget(it) }
                }?.build()
                captureRequest?.let { session.capture(it, null, backgroundHandler) }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                callback(null)
                closeCamera()
            }
        }, backgroundHandler)
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
    }

    fun closeCamera() {
        captureSession?.close()
        cameraDevice?.close()
        imageReader?.close()
        stopBackgroundThread()
    }

    fun getCameraIds(): Array<String> = cameraManager.cameraIdList
}
