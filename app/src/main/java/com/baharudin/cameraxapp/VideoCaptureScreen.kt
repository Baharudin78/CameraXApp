package com.baharudin.cameraxapp

import android.Manifest
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCaptureScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    var recording : Recording? = remember { null }
    var previewView : PreviewView = remember { PreviewView(context) }
    var videoCapture : MutableState<VideoCapture<Recorder>?> = remember{ mutableStateOf(null) }
    var recordingStart : MutableState<Boolean> = remember { mutableStateOf(false) }
    val audioEnable : MutableState<Boolean> = remember { mutableStateOf(false) }
    val cameraSelector : MutableState<CameraSelector> = remember {
        mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }
    LaunchedEffect(previewView) {
        videoCapture.value = context.createVideoCaptureUseCase(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector.value,
            previewView = previewView
        )
    }

    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = { /*TODO*/ },
        permissionsNotAvailableContent = { /*TODO*/ }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = {previewView},
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = {
                    if (!recordingStart.value) {
                        videoCapture.value?.let { videoCapture ->
                            recordingStart.value = true
                            val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
                                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
                            }
                            recording = startRecordingVideo(
                                context = context,
                                filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                                videoCapture = videoCapture,
                                outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir,
                                executor = context.mainExecutor,
                                audioEnabled = audioEnable.value
                            ) { event ->
                                if (event is VideoRecordEvent.Finalize) {
                                    val uri = event.outputResults.outputUri
                                    if (uri != Uri.EMPTY) {
                                        val uriEncoded = URLEncoder.encode(
                                            uri.toString(),
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("${Route.VIDEO_PREVIEW}/$uriEncoded")
                                    }
                                }
                            }
                        }
                    } else{
                        recordingStart.value = false
                        recording?.stop()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (recordingStart.value) R.drawable.ic_stop else R.drawable.ic_record),
                    contentDescription =  "",
                    modifier = Modifier.size(64.dp)
                )
            }
            if (!recordingStart.value) {
                IconButton(onClick = {
                    audioEnable.value = !audioEnable.value
                },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                ) {
                   Icon(
                       painter = painterResource(if (audioEnable.value) R.drawable.ic_mic_on else R.drawable.ic_mic_off),
                       contentDescription = "",
                       modifier = Modifier.size(64.dp)
                   )
                }
            }
            if (!recordingStart.value) {
                IconButton(onClick = {
                    cameraSelector.value =
                        if (cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA
                    lifecycleOwner.lifecycleScope.launch {
                        videoCapture.value = context.createVideoCaptureUseCase(
                            lifecycleOwner = lifecycleOwner,
                            cameraSelector = cameraSelector.value,
                            previewView = previewView
                        )
                    }
                },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_switch),
                        contentDescription = "",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}