package com.android.gatherly.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.android.gatherly.ui.settings.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun saveProfilePictureLocally(
    context: Context,
    bitmap: Bitmap,
    dispatcher: CoroutineDispatcher
): Boolean =
    withContext(dispatcher) {
      try {
        val file = File(context.filesDir, SettingsViewModel.PROFILE_PIC_FILENAME)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        true
      } catch (e: Exception) {
        Log.e("ImageSaver", "Failed to save picture", e)
        false
      }
    }
