package com.android.gatherly.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.android.gatherly.R

@Composable
fun profilePicturePainter(
    pictureUrl: String?,
    placeholder: Painter = painterResource(R.drawable.default_profile_picture)
): Painter {
  return if (!pictureUrl.isNullOrEmpty()) {
    rememberAsyncImagePainter(pictureUrl)
  } else {
    placeholder
  }
}
