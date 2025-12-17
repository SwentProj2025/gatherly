package com.android.gatherly.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.android.gatherly.R

/**
 * Attendee count display components for the Gatherly app.
 *
 * Provides UI components to display the number of attendees for events.
 */

/**
 * Displays the number of attendees for an event with a person icon.
 *
 * @param numberAttendees The number of attendees to display.
 * @param modifier Modifier applied to the root row container.
 */
@Composable
fun BoxNumberAttendees(numberAttendees: Int, modifier: Modifier = Modifier) {

  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = "Attendees",
        tint = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_between_elements)))

    Text(
        text = "$numberAttendees",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium)
  }
}
