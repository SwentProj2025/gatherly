package com.android.gatherly.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner

class CustomTestRunner : AndroidJUnitRunner() {
  override fun onStart() {
    // Bridge the argument to a System Property before tests start
    val timeout =
        InstrumentationRegistry.getArguments().getString("kotlinx.coroutines.test.default_timeout")

    if (timeout != null) {
      System.setProperty("kotlinx.coroutines.test.default_timeout", timeout)
    }

    super.onStart()
  }
}
