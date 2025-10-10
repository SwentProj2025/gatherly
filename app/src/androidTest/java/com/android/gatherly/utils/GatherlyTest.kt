package com.android.gatherly.utils

import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

abstract class GatherlyTest {
  @Before
  open fun setUp() {
    /*ToDosRepositoryProvider.repository = createInitializedRepository()
    HttpClientProvider.client = initializeHTTPClient()
    if (shouldSignInAnounymously) {
        runTest { FirebaseEmulator.auth.signInAnonymously().await() }
    }*/
    runTest { FirebaseEmulator.auth.signInAnonymously().await() }
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }
}
