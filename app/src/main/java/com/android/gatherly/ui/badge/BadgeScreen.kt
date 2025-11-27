package com.android.gatherly.ui.badge

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.compose.foundation.lazy.items
import com.android.gatherly.R


@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel = BadgeViewModel(repository = ProfileRepositoryFirestore(Firebase.firestore, Firebase.storage)),
    navigationActions: NavigationActions? = null,
) {

    val uiState by viewModel.uiState.collectAsState()

    val badges = listOf(
        uiState.badgeTodoCreated,
        uiState.badgeTodoCompleted,
        uiState.badgeEventCreated,
        uiState.badgeEventParticipated,
        uiState.badgeFriendAdded,
        uiState.badgeFocusSessionCompleted
    )


    Scaffold(
        content = {padding ->
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(padding)
            ) {
                items(badges) { triple ->
                    BadgeItem(triple)
                }
            }

        }
    )


}


/**
 * Displays a single Badge item inside a [Card]
 *
 */

@Composable
fun BadgeItem(triple: Triple<String, String, String>) {
    val (title, description, iconPath) = triple

    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.google_logo),
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            // Title + description on the right
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}