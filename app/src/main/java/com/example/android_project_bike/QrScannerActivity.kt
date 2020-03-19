package com.example.android_project_bike

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.util.SparseArray
import android.view.Surface
import android.view.SurfaceControl
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Processor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.util.jar.Manifest


class QrScannerActivity : AppCompatActivity() {

    lateinit var surfaceView: SurfaceView
    lateinit var camera: CameraSource
    lateinit var barcodeDetector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        surfaceView = findViewById<SurfaceView>(R.id.scanner)

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        camera = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(640, 480).build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        "Manifest.permission.CAMERA"
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                try {
                    camera.start(holder)
                } catch (e: IOException) {
                    Log.e("ERROR", "$e")
                }

            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                camera.stop()
            }


        }
        )

        barcodeDetector.setProcessor(object : Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                var qr_codes : SparseArray<Barcode>
                qr_codes = detections.detectedItems

                if (qr_codes.size() != 0) {
                        var vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(1000)

                }

            }


        })
    }
}

