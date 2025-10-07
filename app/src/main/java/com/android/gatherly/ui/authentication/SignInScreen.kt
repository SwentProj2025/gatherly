package com.android.gatherly.ui.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// placeholder Color definitions TODO replace by values in res / define a theme
private val DarkBackground = Color(0xFF1A1D23)
private val ButtonBackground = Color(0xFF2D3139)
private val TextWhite = Color(0xFFFFFFFF)

@Composable
fun SignInScreen() {

  Scaffold(
      containerColor = DarkBackground,
      modifier = Modifier.fillMaxSize(),
      content = { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween) {
              Spacer(modifier = Modifier.height(80.dp))

              Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    WelcomeSection()
                    Spacer(modifier = Modifier.height(60.dp))
                    SignInButtons()
                  }

              SignUpSection()
              Spacer(modifier = Modifier.height(32.dp))
            }
      })
}

@Composable
fun WelcomeSection() {

  Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
    Text(
        text = "Welcome to Gatherly,",
        color = TextWhite,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Your workflow and social events app!",
        color = TextWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.fillMaxWidth())
  }
}

@Composable
fun SignInButtons() {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

    // SignInButton(text = "Continue with EPFL") maybe add later
    SignInButton(text = "Continue with Google")
  }
}

@Composable
fun SignInButton(text: String) {
  Button(
      onClick = { /* TODO*/},
      modifier = Modifier.fillMaxWidth().height(56.dp),
      colors =
          ButtonDefaults.buttonColors(containerColor = ButtonBackground, contentColor = TextWhite),
      shape = RoundedCornerShape(12.dp)) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
      }
}

@Composable
fun SignUpSection() {
  TextButton(onClick = { /* TODO*/}, modifier = Modifier.fillMaxWidth()) {
    Text(
        text = "Don't have an account? Sign up",
        color = TextWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium)
  }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
  SignInScreen()
}
