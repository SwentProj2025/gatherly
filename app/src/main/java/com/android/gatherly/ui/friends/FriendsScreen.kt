package com.android.gatherly.ui.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.model.profile.ProfileRepositoryFirestore
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu_Goback
import com.android.gatherly.utils.GenericViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.android.gatherly.R

object FriendsScreenTestTags {
    const val BUTTON_FIND_FRIENDS = "buttonFindFriends"
    const val FRIEND_ITEM = "friendItems"
}

@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel(
        factory =
            GenericViewModelFactory<FriendsViewModel> {
                FriendsViewModel(
                    repository = ProfileRepositoryFirestore(Firebase.firestore),
                    currentUserId = Firebase.auth.currentUser?.uid ?: ""
                )

            }),
    goBack: () -> Unit,
    findNewFriend: () -> Unit,
) {

    val currentUserIdFromVM = friendsViewModel.currentUserId
    val uiState by friendsViewModel.uiState.collectAsState()
    val friendsList = uiState.friends

    var searchQuery by remember { mutableStateOf("") }

    val filteredFriends = if (searchQuery.isBlank()) {
        friendsList
    } else {
        friendsList.filter { friend ->
            friend.contains(searchQuery, ignoreCase = true)
        }
    }


    LaunchedEffect(currentUserIdFromVM) {
        if (currentUserIdFromVM.isNotBlank()) {
            friendsViewModel.refreshFriends(currentUserIdFromVM)
        }
    }


  Scaffold(
      topBar = {
        TopNavigationMenu_Goback(
            selectedTab = Tab.Friends,
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            goBack = goBack)
      },
      content = { padding ->
          LazyColumn(
              contentPadding = PaddingValues(vertical = 8.dp),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 16.dp)
                      .padding(padding)) {

              if (filteredFriends.isEmpty()) {
                  item {
                      Text(
                          text = stringResource(R.string.friends_empty_list_msg),
                          modifier = Modifier
                              .padding(16.dp))
                  }
              } else {
                  item {
                      OutlinedTextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          modifier = Modifier
                              .fillMaxWidth()
                              .padding(vertical = 8.dp),
                          placeholder = {
                              Text(
                                  text = stringResource(R.string.friends_search_bar_label),
                                  modifier = Modifier.padding(16.dp)) },
                          singleLine = true,
                          shape = RoundedCornerShape(12.dp)
                      )

                  }

                  items(filteredFriends.size) { index ->
                      val friend : String = filteredFriends[index]
                      FriendItem(friend = friend,
                          unfollow = { friendsViewModel.unfollowFriend(currentUserIdFromVM, friend ?: "") } )

                  }
              }

              item {
                  Button(
                      onClick = { findNewFriend() },
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(80.dp)
                              .padding(vertical = 12.dp),
                      shape = RoundedCornerShape(12.dp),
                      colors = buttonColors(containerColor = Color(0xFF9ADCE5))) {

                      Text(
                          text = stringResource(R.string.find_friends_button_label),
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Medium,
                          color = MaterialTheme.colorScheme.onPrimary)
                  }
              }

          }

      })
}

@Composable
fun FriendItem(friend: String, unfollow: () -> Unit) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.primary),
        modifier =
            Modifier.clickable(onClick = unfollow)
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // currently a placeholder image
            contentDescription = "Profile picture of ${friend}",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(16.dp))

            Button(
            onClick = unfollow,
            modifier = Modifier.wrapContentWidth()
        ) {
            Text("Unfollow")
        }
        }
    }
}
