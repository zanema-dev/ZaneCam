package com.example.ui

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreview(
    zoomRatio: Float,
    aeAfLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var activeCamera = remember<Camera?> { null }

    // Re-bind configuration on layout creation
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Clear any lingering target binds
                cameraProvider.unbindAll()
                // Bind to lifecycle and reference camera controller
                activeCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Capture volume-controlled zoom inputs
    LaunchedEffect(zoomRatio) {
        activeCamera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    // Handle AE/AF Lock states
    LaunchedEffect(aeAfLocked) {
        activeCamera?.let { camera ->
            camera.cameraControl.enableTorch(false)
            if (aeAfLocked) {
                // Pin manual elements to locked capture settings
                camera.cameraControl.cancelFocusAndMetering()
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
