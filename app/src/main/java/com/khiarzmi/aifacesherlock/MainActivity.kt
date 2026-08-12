package com.khiarzmi.aifacesherlock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> setContent { FaceSherlockScreen(granted) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        setContent { FaceSherlockScreen(granted) }
        if (!granted) cameraPermission.launch(Manifest.permission.CAMERA)
    }

    @Composable
    private fun FaceSherlockScreen(cameraGranted: Boolean) {
        var result by remember { mutableStateOf("Arahkan wajah ke kamera") }
        var scanning by remember { mutableStateOf(true) }

        MaterialTheme {
            Box(Modifier.fillMaxSize()) {
                if (cameraGranted) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        scanning = scanning,
                        onFaceDetected = { count ->
                            result = if (count > 0) "Wajah terdeteksi — mencari kecocokan..." else "Arahkan wajah ke kamera"
                        }
                    )
                } else {
                    Text("Izin kamera diperlukan", Modifier.align(Alignment.Center))
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(result)
                    Button(onClick = {
                        scanning = false
                        result = "Gagal — tidak ditemukan kemiripan"
                    }) { Text("Cek Kemiripan") }
                }
            }
        }
    }

    @Composable
    private fun CameraPreview(
        modifier: Modifier,
        scanning: Boolean,
        onFaceDetected: (Int) -> Unit
    ) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val view = PreviewView(context)
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    val provider = future.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                    val options = FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).build()
                    val detector = FaceDetection.getClient(options)
                    val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    analysis.setAnalyzer(executor) { proxy ->
                        if (!scanning) { proxy.close(); return@setAnalyzer }
                        val media = proxy.image
                        if (media == null) { proxy.close(); return@setAnalyzer }
                        val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                        detector.process(image)
                            .addOnSuccessListener { onFaceDetected(it.size) }
                            .addOnCompleteListener { proxy.close() }
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
                }, ContextCompat.getMainExecutor(context))
                view
            }
        )
    }
}
