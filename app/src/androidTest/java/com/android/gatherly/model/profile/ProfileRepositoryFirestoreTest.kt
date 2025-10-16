package com.android.gatherly.model.profile

import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests for [ProfileRepositoryFirestore] using the Firebase Emulators.
 *
 * These tests assume:
 * - The Firestore and Auth emulators are running locally.
 */
class ProfileRepositoryFirestoreTest : FirestoreGatherlyProfileTest() {

  @Test
  fun initProfileIfMissing_createsProfileWhenAbsent() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid

    val created = repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

    assertTrue(created)
    val profile = repository.getProfileByUid(uid)
    assertNotNull(profile)
    assertEquals(uid, profile!!.uid)
    assertEquals("default.png", profile.profilePicture)
    assertEquals("", profile.username)
  }

  @Test
  fun initProfileIfMissing_returnsFalseWhenAlreadyExists() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid

    repository.initProfileIfMissing(uid, "photo1.png")
    val result = repository.initProfileIfMissing(uid, "photo2.png")

    assertFalse(result)
    val profile = repository.getProfileByUid(uid)
    assertEquals("photo1.png", profile!!.profilePicture) // unchanged
  }

  @Test
  fun updateProfile_updatesExistingProfile() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "pic1.png")

    val updated =
        Profile(uid = uid, name = "Alice", school = "EPFL", profilePicture = "updated.png")
    repository.updateProfile(updated)

    val fetched = repository.getProfileByUid(uid)
    assertEquals("Alice", fetched!!.name)
    assertEquals("EPFL", fetched.school)
    assertEquals("updated.png", fetched.profilePicture)
  }

  @Test(expected = NoSuchElementException::class)
  fun updateProfile_throwsIfNotExists() = runTest {
    val fake = Profile(uid = "nonexistent", name = "Ghost")
    repository.updateProfile(fake)
  }

  @Test
  fun deleteProfile_removesItFromDatabase() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "p.png")

    assertTrue(repository.isUidRegistered(uid))
    repository.deleteProfile(uid)
    assertFalse(repository.isUidRegistered(uid))
  }

  @Test
  fun isUidRegistered_returnsTrueOnlyIfExists() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    assertFalse(repository.isUidRegistered(uid))

    repository.initProfileIfMissing(uid, "pic.png")
    assertTrue(repository.isUidRegistered(uid))
  }

  @Test
  fun searchProfilesByNamePrefix_returnsMatchingProfiles() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore

    // --- USER A ---
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore)
    repoA.initProfileIfMissing(userAUid, "defaultA.png")
    repoA.updateProfile(Profile(uid = userAUid, name = "Alice", profilePicture = "a.png"))

    // --- USER B ---
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore)
    repoB.initProfileIfMissing(userBUid, "defaultB.png")
    repoB.updateProfile(Profile(uid = userBUid, name = "Alex", profilePicture = "b.png"))

    // --- SEARCH (performed by User B for simplicity) ---
    val result = repoB.searchProfilesByNamePrefix("Al")

    assertTrue(result.isNotEmpty())
    assertTrue(result.all { it.name.startsWith("Al", ignoreCase = true) })
  }

  @Test
  fun registerUsername_failsIfProfileDoesNotExist() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    try {
      repository.registerUsername(uid, "newuser")
      fail("Expected IllegalStateException because profile does not exist yet")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("profile creation"))
    }
  }

  @Test
  fun registerUsername_updatesProfileWhenProfileExists() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    val created = repository.initProfileIfMissing(uid, "pic.png")
    println("initProfileIfMissing result: $created")
    val snap = FirebaseEmulator.firestore.collection("profiles").document(uid).get().await()
    println("Firestore raw document exists? ${snap.exists()}")
    println("Raw Firestore data: ${snap.data}")
    delay(500)
    println("getProfileByUidResults: ${repository.getProfileByUid(uid)}")
    println("getprofilebyuid returns null ?: ${repository.getProfileByUid(uid) == null}")

    println("🔹 Calling registerUsername() now...")
    val success = repository.registerUsername(uid, "alice")
    println("registerUsername() returned: $success")
    assertTrue(success)

    val profile = repository.getProfileByUid(uid)
    assertNotNull(profile)
    assertEquals("alice", profile!!.username)
  }

  @Test
  fun updateUsername_replacesOldUsername() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "pic.png")
    repository.registerUsername(uid, "oldname")

    val success = repository.updateUsername(uid, "oldname", "newname")
    assertTrue(success)

    val updatedProfile = repository.getProfileByUid(uid)
    assertEquals("newname", updatedProfile!!.username)
  }

  @Test
  fun getProfileByUsername_returnsCorrectProfile() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "photo.png")
    repository.registerUsername(uid, "charlie")

    val fetched = repository.getProfileByUsername("charlie")
    assertNotNull(fetched)
    assertEquals(uid, fetched!!.uid)
  }

  @Test
  fun userCannotEditAnotherUserProfile_dueToRules() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore

    // User A creates a profile
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore)
    repoA.initProfileIfMissing(userAUid, "alice.png")

    // Switch to User B
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore)

    // Attempt to edit Alice's profile → should fail
    val unauthorized = Profile(uid = userAUid, name = "Hacked Alice", profilePicture = "evil.png")
    try {
      repoB.updateProfile(unauthorized)
      fail("Expected Firestore PERMISSION_DENIED when editing another user's profile")
    } catch (e: Exception) {
      assertTrue(e.message?.contains("PERMISSION_DENIED") == true)
    }
  }

  @Test
  fun userCanReadOtherUserProfile_whenAuthenticated() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore)
    repoA.initProfileIfMissing(userAUid, "alice.png")

    // User B
    auth.signOut()
    auth.signInAnonymously().await()
    val repoB = ProfileRepositoryFirestore(firestore)

    val fetched = repoB.getProfileByUid(userAUid)
    assertNotNull(fetched)
  }

  @Test
  fun isUsernameAvailable_behavesCorrectly() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "photo.png")

    // Valid and available username
    assertTrue(repository.isUsernameAvailable("validname"))

    // Invalid username pattern
    assertFalse(repository.isUsernameAvailable("a!")) // invalid due to punctuation
    assertFalse(repository.isUsernameAvailable("ab")) // invalid due to length < 3

    // Already taken username
    repository.registerUsername(uid, "takenuser")
    assertFalse(repository.isUsernameAvailable("takenuser"))
  }

}
