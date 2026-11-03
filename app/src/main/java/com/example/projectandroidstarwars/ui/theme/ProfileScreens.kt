package com.example.projectandroidstarwars.ui.theme

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.projectandroidstarwars.viewmodel.ProfileViewModel

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onEdit: () -> Unit
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    if (state.loading) {
        ProfileLoading()
        return
    }

    if (!state.loaded) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.error ?: "Профиль пока не загружен."
            )

            Button(onClick = viewModel::load) {
                Text("Повторить")
            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileAvatar(
            uri = state.profile.avatarUri
        )

        Text(
            text = state.profile.fullName.ifBlank {
                "Имя не указано"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = {
                viewModel.beginEditing()
                onEdit()
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать профиль")
        }

        OutlinedButton(
            onClick = {
                viewModel.downloadResume { uri ->
                    openResume(
                        context = context,
                        uri = uri,
                        onError = viewModel::reportError
                    )
                }
            },
            enabled = !state.busy &&
                    state.profile.resumeUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (state.busy) {
                    "Скачивание…"
                } else {
                    "Резюме"
                }
            )
        }

        if (state.profile.resumeUrl.isBlank()) {
            Text(
                text = "Добавь ссылку на резюме в редактировании профиля.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.busy) {
            CircularProgressIndicator()
        }

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun EditProfileRoute(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    var showPhotoSource by rememberSaveable {
        mutableStateOf(false)
    }

    var accessGranted by remember {
        mutableStateOf(hasPhotoPermission(context))
    }

    BackHandler(enabled = state.busy) {}

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.cameraFinished(success)
    }

    val launchCamera: () -> Unit = {
        viewModel.prepareCamera { uri ->
            try {
                cameraLauncher.launch(Uri.parse(uri))
            } catch (_: ActivityNotFoundException) {
                viewModel.cameraFinished(false)

                viewModel.reportError(
                    "На устройстве не найдено приложение камеры."
                )
            } catch (_: SecurityException) {
                viewModel.cameraFinished(false)

                viewModel.reportError(
                    "Нет разрешения на использование камеры."
                )
            }
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                launchCamera()
            } else {
                viewModel.reportError(
                    "Доступ к камере не предоставлен."
                )
            }
        }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importAvatar(uri.toString())
        }
    }

    val photoPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.values.any { it } ||
                    hasPhotoPermission(context)

            accessGranted = granted

            if (!granted) {
                Toast.makeText(
                    context,
                    "Доступ к фотографиям не предоставлен.",
                    Toast.LENGTH_SHORT
                ).show()

                onBack()
            }
        }

    LaunchedEffect(Unit) {
        if (hasPhotoPermission(context)) {
            accessGranted = true
        } else {
            photoPermissionLauncher.launch(
                requiredPhotoPermissions()
            )
        }
    }

    if (!accessGranted || state.loading) {
        ProfileLoading()
        return
    }

    if (!state.loaded) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = state.error ?: "Не удалось загрузить профиль."
            )

            Button(onClick = viewModel::load) {
                Text("Повторить")
            }

            TextButton(onClick = onBack) {
                Text("Назад")
            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileAvatar(
            uri = state.draft.avatarUri,
            modifier = Modifier.clickable(
                enabled = !state.busy
            ) {
                showPhotoSource = true
            }
        )

        TextButton(
            onClick = {
                showPhotoSource = true
            },
            enabled = !state.busy
        ) {
            Text("Изменить фотографию")
        }

        OutlinedTextField(
            value = state.draft.fullName,
            onValueChange = viewModel::updateFullName,
            label = {
                Text("ФИО")
            },
            singleLine = true,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.draft.resumeUrl,
            onValueChange = viewModel::updateResumeUrl,
            label = {
                Text("Ссылка на резюме")
            },
            supportingText = {
                Text("Прямая HTTPS-ссылка на PDF-файл.")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            ),
            singleLine = true,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.busy) {
            CircularProgressIndicator()
        }

        Button(
            onClick = {
                viewModel.save(onSaved = onBack)
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (state.busy) {
                    "Подождите…"
                } else {
                    "Готово"
                }
            )
        }

        TextButton(
            onClick = onBack,
            enabled = !state.busy
        ) {
            Text("Отмена")
        }
    }

    if (showPhotoSource) {
        AlertDialog(
            onDismissRequest = {
                showPhotoSource = false
            },
            title = {
                Text("Фотография профиля")
            },
            text = {
                Text("Откуда взять фотографию?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoSource = false

                        try {
                            galleryLauncher.launch("image/*")
                        } catch (_: ActivityNotFoundException) {
                            viewModel.reportError(
                                "Не найдено приложение для выбора фото."
                            )
                        }
                    }
                ) {
                    Text("Галерея")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoSource = false

                        val cameraGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                        if (cameraGranted) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    }
                ) {
                    Text("Камера")
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatar(
    uri: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(144.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (uri.isBlank()) {
            Text(
                text = "Фото",
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            AsyncImage(
                model = Uri.parse(uri),
                contentDescription = "Фотография профиля",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProfileLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun requiredPhotoPermissions(): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= 34 -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        }

        Build.VERSION.SDK_INT >= 33 -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
        }

        else -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
}

private fun hasPhotoPermission(context: Context): Boolean {
    return requiredPhotoPermissions().any { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun openResume(
    context: Context,
    uri: String,
    onError: (String) -> Unit
) {
    val contentUri = Uri.parse(uri)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/pdf")

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        clipData = ClipData.newRawUri(
            "Резюме",
            contentUri
        )
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        onError(
            "PDF скачан, но на устройстве нет приложения для его просмотра."
        )
    } catch (_: SecurityException) {
        onError(
            "Не удалось предоставить просмотрщику доступ к PDF."
        )
    }
}