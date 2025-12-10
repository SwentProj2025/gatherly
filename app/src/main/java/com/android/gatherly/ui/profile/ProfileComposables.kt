package com.android.gatherly.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.android.gatherly.R
import com.android.gatherly.model.profile.ProfileStatus
import com.android.gatherly.utils.profilePicturePainter

/**
 * Profile UI Components
 *
 * This file contains composable functions related to displaying a user's profile information,
 * including:
 * - A circular profile picture
 * - A small colored status indicator
 * - A combined layout that overlays the status indicator on the profile picture
 *
 * These components are shared across profile-related screens and include built-in testTags to
 * support UI testing at both the composable and screen levels.
 */

/**
 * Test tags for profile-related composables.
 *
 * These tags are used across different UI test types (unit-level & screen-level) to ensure
 * consistent and stable test references.
 */
object ProfileComposablesTestTags {
  const val PROFILE_PICTURE = "profilePicture"
  const val USER_STATUS = "user_status"
}

/**
 * Displays a circular profile picture using the given URL.
 *
 * The image is clipped to a circle, includes a border, and defaults to a size defined in resources
 * unless overridden.
 *
 * @param modifier Optional [Modifier] for layout or styling.
 * @param pictureUrl The URL for the user’s profile picture. If null, a placeholder is shown.
 * @param testTag A tag that will be applied to the image for testing purposes.
 * @param size The diameter of the profile picture. Defaults to a resource value.
 */
@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    pictureUrl: String?,
    testTag: String = ProfileComposablesTestTags.PROFILE_PICTURE,
    size: Dp = dimensionResource(id = R.dimen.profile_pic_size)
) {
  Image(
      painter = profilePicturePainter(pictureUrl),
      contentDescription = stringResource(R.string.profile_picture_description),
      modifier =
          modifier
              .size(size)
              .border(
                  width = dimensionResource(id = R.dimen.profile_pic_border),
                  color = MaterialTheme.colorScheme.outline,
                  shape = CircleShape)
              .clip(CircleShape)
              .testTag(testTag),
      contentScale = ContentScale.Crop)
}

/**
 * Small colored status dot used to represent a user's presence state. (Green = Online, Red =
 * Offline, Blue = Focused)
 *
 * @param status The current [ProfileStatus] to display.
 * @param modifier Optional modifier for positioning.
 * @param size The diameter of the indicator.
 */
@Composable
fun StatusIndicator(
    status: ProfileStatus,
    modifier: Modifier = Modifier,
    size: Dp,
    testTag: String = ProfileComposablesTestTags.USER_STATUS
) {
  val color =
      when (status) {
        ProfileStatus.ONLINE -> Color.Green
        ProfileStatus.FOCUSED -> Color.Blue
        ProfileStatus.OFFLINE -> Color.Red
      }

  Box(modifier = modifier.size(size).clip(CircleShape).background(color).testTag(testTag))
}

/**
 * Displays a profile picture with a status indicator positioned at the bottom-right.
 *
 * This composable combines [ProfilePicture] and [StatusIndicator] into a unified UI element.
 *
 * @param profilePictureUrl The URL of the user's profile picture.
 * @param status The current [ProfileStatus] to display (Online, Offline, Focused).
 * @param modifier Optional [Modifier] for external layout or styling.
 * @param profilePictureTestTag Test tag applied to the internal [ProfilePicture].
 * @param statusTestTag Test tag applied to the internal [StatusIndicator].
 * @param size The diameter of the profile picture container.
 */
@Composable
fun ProfilePictureWithStatus(
    profilePictureUrl: String,
    status: ProfileStatus,
    modifier: Modifier = Modifier,
    profilePictureTestTag: String = ProfileComposablesTestTags.PROFILE_PICTURE,
    statusTestTag: String = ProfileComposablesTestTags.USER_STATUS,
    size: Dp = dimensionResource(id = R.dimen.profile_pic_size)
) {
  Box(modifier = modifier.size(size)) {
    ProfilePicture(pictureUrl = profilePictureUrl, testTag = profilePictureTestTag)

    StatusIndicator(
        status = status,
        modifier = Modifier.align(Alignment.BottomEnd).testTag(statusTestTag),
        size = size * 0.25f)
  }
}
