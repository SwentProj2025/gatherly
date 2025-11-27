package com.android.gatherly.ui.map

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import com.android.gatherly.model.event.EventsLocalRepository
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class MapScreenCoverageTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  /**
   * Verifies that when the permission launcher callback receives a "Granted" result, the ViewModel
   * correctly triggers location updates and camera initialization.
   */
  @Test
  fun permissionCallback_whenGranted_callsStartLocationUpdates() {
    // =========================================================================================
    // STEP 1: Dependencies & Spy Creation
    // =========================================================================================
    val todoRepo = ToDosLocalRepository()
    val eventsRepo = EventsLocalRepository()
    val locationClient: FusedLocationProviderClient? = null

    val realViewModel = MapViewModel(todoRepo, eventsRepo, locationClient)
    val spyViewModel = spyk(realViewModel)

    // =========================================================================================
    // STEP 2: Behavior Stubbing
    // =========================================================================================
    every { spyViewModel.startLocationUpdates(any()) } returns Unit
    coEvery { spyViewModel.initialiseCameraPosition(any()) } returns Unit

    // =========================================================================================
    // STEP 3: The Fake Registry (The Interceptor)
    // =========================================================================================
    var wasLaunchCalled = false
    var requestedPermissions = emptyArray<String>()

    val fakeRegistry =
        object : ActivityResultRegistry() {
          override fun <I, O> onLaunch(
              requestCode: Int,
              contract: ActivityResultContract<I, O>,
              input: I,
              options: ActivityOptionsCompat?
          ) {
            wasLaunchCalled = true
            @Suppress("UNCHECKED_CAST")
            requestedPermissions = input as Array<String>

            // SIMULATION: The system dialog replies "ALLOW" immediately
            dispatchResult(
                requestCode,
                mapOf(
                    Manifest.permission.ACCESS_FINE_LOCATION to true,
                    Manifest.permission.ACCESS_COARSE_LOCATION to true))
          }
        }

    val registryOwner =
        object : ActivityResultRegistryOwner {
          override val activityResultRegistry = fakeRegistry
        }

    // =========================================================================================
    // STEP 4: Execution & Composition Injection
    // =========================================================================================

    compose.setContent {
      CompositionLocalProvider(
          androidx.activity.compose.LocalActivityResultRegistryOwner provides registryOwner) {
            MapScreen(
                viewModel = spyViewModel, isLocationPermissionGrantedProvider = { _ -> false })
          }
    }

    // =========================================================================================
    // STEP 5: Verification
    // =========================================================================================
    compose.waitForIdle()

    // 1. Verify the screen thought permission was missing and tried to launch the request
    assert(wasLaunchCalled) { "Expected permission launcher to be launched" }

    // 2. Verify we asked for the correct permissions
    assert(requestedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
    assert(requestedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))

    // 3. Verify that after the fake registry said "YES", the viewmodel started updates
    verify(exactly = 1) { spyViewModel.startLocationUpdates(any()) }
    coVerify(exactly = 1) { spyViewModel.initialiseCameraPosition(any()) }
  }
}
