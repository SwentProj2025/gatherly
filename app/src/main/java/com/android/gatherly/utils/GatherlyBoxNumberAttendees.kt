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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Helper function : Display the number of attendees a event got */
@Composable
fun BoxNumberAttendees(numberAttendees: Int, modifier: Modifier = Modifier) {

  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = "Attendees",
        tint = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.width(6.dp))

    Text(
        text = "$numberAttendees",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium)
  }
}
