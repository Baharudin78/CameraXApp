package com.baharudin.cameraxapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baharudin.cameraxapp.Route.VIDEO_PREVIEW_ARG
import com.baharudin.cameraxapp.ui.theme.CameraXAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraXAppTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination =  Route.VIDEO
                    ) {
                        composable(Route.VIDEO) {
                            //video capture screen
                            VideoCaptureScreen(navController = navController)
                        }
                        composable(Route.VIDEO_PREVIEW_FULL_ROUTE) {
                            val uri = it.arguments?.getString(VIDEO_PREVIEW_ARG) ?: ""
                            //VideoPreviewScreen
                            VideoPreviewScreen(uri = uri)
                        }
                    }
                }
            }
        }
    }
}

object Route {
    const val VIDEO = "video"
    const val VIDEO_PREVIEW_FULL_ROUTE ="video_preview_route"
    const val VIDEO_PREVIEW = "video_preview"
    const val VIDEO_PREVIEW_ARG ="url"
}
