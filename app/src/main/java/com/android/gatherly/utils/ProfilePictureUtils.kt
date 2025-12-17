package com.android.gatherly.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.android.gatherly.R

/**
 * Profile picture utilities for the Gatherly app.
 *
 * Provides functionality to load profile pictures from URLs with fallback to placeholder images.
 */

/**
 * Returns a Painter for displaying a profile picture, loading from a URL if available.
 *
 * Uses Coil's async image loading to fetch remote profile pictures. If the URL is null or empty,
 * returns the provided placeholder painter instead.
 *
 * @param pictureUrl The URL of the profile picture to load, or null/empty for placeholder.
 * @param placeholder The fallback painter to use when no URL is provided (default: app's default
 *   profile picture).
 * @return A Painter that either loads the remote image or displays the placeholder.
 */
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
