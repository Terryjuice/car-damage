package com.cardamageai.feature.camera

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (String) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    when {
        cameraPermission.status.isGranted -> {
            CameraContent(
                onImageCaptured = onImageCaptured,
                onHistoryClick = onHistoryClick,
                viewModel = viewModel
            )
        }
        else -> {
            PermissionDeniedContent(
                cameraPermission = cameraPermission
            )
        }
    }
}

@Composable
private fun CameraContent(
    onImageCaptured: (String) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    DisposableEffect(lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            controller = cameraController,
            modifier = Modifier.fillMaxSize()
        )
        
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Text(
                text = "Наведите камеру на повреждение автомобиля",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        IconButton(
            onClick = onHistoryClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "История"
            )
        }

        FloatingActionButton(
            onClick = {
                capturePhoto(
                    controller = cameraController,
                    context = context,
                    onImageCaptured = onImageCaptured
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Сделать фото"
            )
        }
        
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDeniedContent(
    cameraPermission: PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Для работы приложения необходимо разрешение на использование камеры",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = { cameraPermission.launchPermissionRequest() }
        ) {
            Text("Предоставить разрешение")
        }
    }
}

private fun capturePhoto(
    controller: LifecycleCameraController,
    context: android.content.Context,
    onImageCaptured: (String) -> Unit
) {
    val outputDirectory = File(context.cacheDir, "camera_photos")
    if (!outputDirectory.exists()) {
        outputDirectory.mkdirs()
    }

    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                // Handle error
            }
        }
    )
}