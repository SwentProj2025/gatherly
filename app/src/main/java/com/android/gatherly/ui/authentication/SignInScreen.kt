package com.android.gatherly.ui.authentication


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun SignInScreen(

) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Sign In Screen Placeholder")
    }

}


@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen()
}