package com.android.gatherly.utils

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.model.todo.ToDosRepositoryFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule

/**
 * Base class for Firestore-based Android tests using the Firebase Emulator Suite.
 *
 * Before running, start the emulators: firebase emulators:start
 */
open class FirestoreGatherlyTest {

  protected lateinit var repository: ToDosRepository

  // Example todos used in tests
  protected val todo1 =
      ToDo(
          uid = "1",
          name = "Buy groceries",
          description = "Milk, eggs, bread",
          assigneeName = "Alice",
          dueDate = Timestamp.now(),
          dueTime = null,
          location = null,
          status = ToDoStatus.ONGOING,
          ownerId = "test-user")

  protected val todo2 = todo1.copy(uid = "2", name = "Walk the dog")
  protected val todo3 = todo1.copy(uid = "3", name = "Read a book")

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  open fun setUp() {
    if (!FirebaseEmulator.isRunning) {
      error("Firebase emulator must be running! Use: firebase emulators:start")
    }
    FirebaseEmulator.auth.signInAnonymously()
    repository = ToDosRepositoryFirestore(FirebaseEmulator.firestore)
    runTest { clearUserTodos() }
  }

  @After
  open fun tearDown() {
    runTest { clearUserTodos() }
    FirebaseEmulator.clearFirestoreEmulator()
  }

  /** Deletes all todos for the current test user in the emulator Firestore */
  protected suspend fun clearUserTodos() {
    val user = FirebaseEmulator.auth.currentUser ?: return
    val todos =
        FirebaseEmulator.firestore
            .collection("users")
            .document(user.uid)
            .collection("todos")
            .get()
            .await()

    val batch = FirebaseEmulator.firestore.batch()
    todos.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    Log.d("FirestoreGatherlyTest", "Cleared ${todos.size()} todos for user ${user.uid}")
  }

  /** Returns the current count of todos for this user. */
  protected suspend fun getTodosCount(): Int {
    val user = FirebaseEmulator.auth.currentUser ?: return 0
    val snap =
        FirebaseEmulator.firestore
            .collection("users")
            .document(user.uid)
            .collection("todos")
            .get()
            .await()
    return snap.size()
  }
}
