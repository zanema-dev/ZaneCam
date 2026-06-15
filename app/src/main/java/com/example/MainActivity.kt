package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CameraScreen
import com.example.ui.CameraViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.ui.theme.PureBlack
                ) {
                    CameraScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Capture volume down & up clicks for smooth physical lens zooming
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.onVolumeUpPressed()
                true // consumed
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.onVolumeDownPressed()
                true // consumed
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
