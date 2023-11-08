package com.example.karsoftrivojyulduz.util.constant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class Permission {

    fun hasReadMediaImagesPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("NewApi")
    fun hasManageExternalStoragePermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasReadExternalStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(cameraRequestPermissionLauncher: ActivityResultLauncher<String>) {
        cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestReadExternalStoragePermission(galleryRequestPermissionLauncher: ActivityResultLauncher<String>) {
        galleryRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun requestReadMediaImagesPermission(galleryRequestPermissionLauncher: ActivityResultLauncher<String>) {
        galleryRequestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    private fun requestManageExternalStoragePermission(
        context: Context,
        galleryResultLauncher: ActivityResultLauncher<Intent>
    ) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(String.format("package:%s", context.packageName))
            galleryResultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            galleryResultLauncher.launch(intent)
        }
    }

    fun requestGalleryPermission(
        context: Context,
        galleryRequestPermissionLauncher: ActivityResultLauncher<String>,
        galleryResultLauncher: ActivityResultLauncher<Intent>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permission().requestReadMediaImagesPermission(galleryRequestPermissionLauncher)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Permission().requestManageExternalStoragePermission(
                context,
                galleryResultLauncher
            )
        } else {
            Permission().requestReadExternalStoragePermission(galleryRequestPermissionLauncher)
        }
    }
}