package com.android.gatherly.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.gatherly.R
import com.android.gatherly.model.group.Group
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.utils.profilePicturePainter

const val MAX_MEMBERS_DISPLAYED = 3

/**
 * Composable that shows an overview of groups with their members' profile pictures and member
 * count.
 *
 * @param groupsToMembers A map where each key is a Group and the value is a list of Profiles
 *   representing the members of that group.
 * @param modifier Modifier to be applied to the GroupsOverview container.
 */
@Composable
fun GroupsOverview(groupsToMembers: Map<Group, List<Profile>>, modifier: Modifier = Modifier) {
  val profilePictureSize = dimensionResource(id = R.dimen.profile_pic_size_small)
  val smallSpacing = dimensionResource(id = R.dimen.spacing_between_fields_smaller_regular)
  val regularSpacing = dimensionResource(id = R.dimen.spacing_between_fields_regular)
  val dividerThickness = dimensionResource(id = R.dimen.group_overview_horizontal_divider_thickness)
  val avatarSpacing = dimensionResource(id = R.dimen.group_overview_avatar_spacing)
  val horizontalPadding = dimensionResource(id = R.dimen.group_overview_row_horizontal_padding)
  val borderWidth = dimensionResource(id = R.dimen.group_overview_avatar_border_width)
  val roundedCorner = dimensionResource(id = R.dimen.rounded_corner_shape_large)
  val pictureContentDescription = stringResource(R.string.profile_picture_description)
  val oneMember = stringResource(R.string.group_members_text_singular)
  val multipleMembers = stringResource(R.string.group_members_text_plural)

  // Width needed for 3 avatars + spacing between them
  val maxAvatarWidth =
      profilePictureSize * MAX_MEMBERS_DISPLAYED +
          avatarSpacing * (MAX_MEMBERS_DISPLAYED - 1) // two gaps between 3 images

  Column(
      modifier =
          modifier
              .background(
                  MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(roundedCorner))
              .testTag(ProfileScreenTestTags.GROUPS_OVERVIEW_CONTAINER)) {
        val groups = groupsToMembers.keys.toList()
        groups.forEachIndexed { index, group ->
          val groupSize = group.memberIds.size
          val memberText = if (groupSize == 1) oneMember else multipleMembers
          val membersProfile = groupsToMembers[group] ?: emptyList()
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = horizontalPadding, vertical = smallSpacing)
                      .testTag("${ProfileScreenTestTags.GROUP_ROW}_$index"),
              verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.width(maxAvatarWidth),
                    verticalAlignment = Alignment.CenterVertically) {
                      membersProfile.take(MAX_MEMBERS_DISPLAYED).forEach { member ->
                        Image(
                            painter = profilePicturePainter(member.profilePicture),
                            contentDescription = pictureContentDescription,
                            modifier =
                                Modifier.size(profilePictureSize)
                                    .clip(CircleShape)
                                    .border(
                                        borderWidth,
                                        MaterialTheme.colorScheme.outline,
                                        CircleShape))
                        Spacer(modifier = Modifier.width(avatarSpacing))
                      }
                    }

                Spacer(modifier = Modifier.width(regularSpacing))

                Column(
                    modifier =
                        Modifier.weight(
                            integerResource(R.integer.groups_column_weight).toFloat())) {
                      Text(
                          text = group.name,
                          modifier =
                              Modifier.testTag("${ProfileScreenTestTags.GROUP_ROW_NAME}_$index"),
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.SemiBold)
                      Text(
                          text = "$groupSize $memberText",
                          modifier =
                              Modifier.testTag(
                                  "${ProfileScreenTestTags.GROUP_ROW_MEMBER_COUNT}_$index"),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
              }

          if (index < groups.lastIndex) {
            HorizontalDivider(
                thickness = dividerThickness, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
}
