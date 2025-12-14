package com.android.gatherly.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.android.gatherly.R

@Composable
fun LoadingAnimation(loadingMessage: String, innerPadding: PaddingValues) {
  Box(
      modifier = Modifier.padding(innerPadding).fillMaxSize(),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(modifier = Modifier.testTag(LoadingAnimationTestTags.LOADING))

          Spacer(Modifier.size(dimensionResource(R.dimen.spacing_between_fields_medium)))

          Text(
              text = loadingMessage,
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.testTag(LoadingAnimationTestTags.LOADING_TEXT))
        }
      }
}

object LoadingAnimationTestTags {
  const val LOADING = "circularAnimationLoading"
  const val LOADING_TEXT = "textMessageLoading"
}
