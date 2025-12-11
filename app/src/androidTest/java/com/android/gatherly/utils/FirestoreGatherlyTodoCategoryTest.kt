package com.android.gatherly.utils

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.android.gatherly.model.todoCategory.ToDoCategory
import com.android.gatherly.model.todoCategory.ToDoCategoryRepository
import com.android.gatherly.model.todoCategory.ToDoCategoryRepositoryFirestore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base class for Firestore-based Android tests using the Firebase Emulator Suite.
 *
 * Before running, start the emulators: firebase emulators:start
 */
open class FirestoreGatherlyTodoCategoryTest {

  protected lateinit var repository: ToDoCategoryRepository

  protected val tag1 = ToDoCategory(name = "Sport", color = Color(0xFFFFFFFF))

  @Before
  open fun setUp() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }
    FirebaseEmulator.auth.signInAnonymously().await()
    repository = ToDoCategoryRepositoryFirestore(FirebaseEmulator.firestore)
    clearUserTodoCategories()
  }

  @After
  open fun tearDown() {
    runTest(timeout = 120.seconds) { clearUserTodoCategories() }
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /** Deletes all todos for the current test user in the emulator Firestore */
  protected suspend fun clearUserTodoCategories() {
    val user = FirebaseEmulator.auth.currentUser ?: return
    val todoCategory =
        FirebaseEmulator.firestore
            .collection("users")
            .document(user.uid)
            .collection("todoCategories")
            .get()
            .await()

    val batch = FirebaseEmulator.firestore.batch()
    todoCategory.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    Log.d(
        "FirestoreGatherlyTodoCategoryTest",
        "Cleared ${todoCategory.size()} " + "todoCategories for user ${user.uid}")
  }

  /** Returns the current count of todoCategory for this user. */
  protected suspend fun getTodoCategoryCount(): Int {
    val user = FirebaseEmulator.auth.currentUser ?: return 0
    val snap =
        FirebaseEmulator.firestore
            .collection("users")
            .document(user.uid)
            .collection("todoCategories")
            .get()
            .await()
    return snap.size()
  }
}
