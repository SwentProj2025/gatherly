package com.android.gatherly.model.profile

import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests for [ProfileRepositoryFirestore] using the Firebase Emulator Suite.
 *
 * These tests assume:
 * - The Firestore and Auth emulators are running locally.
 */
class ProfileRepositoryFirestoreTest : FirestoreGatherlyProfileTest() {

  @Test
  fun add_and_getProfile_works() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    val profile = Profile(uid = uid, name = "Alice", school = "EPFL", profilePicture = "pic.png")

    repository.addProfile(profile)
    val retrieved = repository.getProfileByUid(uid)

    assertNotNull(retrieved)
    assertEquals("Alice", retrieved!!.name)
    assertEquals("EPFL", retrieved.school)
    assertEquals("pic.png", retrieved.profilePicture)
  }

  @Test
  fun updateProfile_updates_existing_profile() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    val profile = Profile(uid = uid, name = "Alice", school = "EPFL", profilePicture = "pic1.png")
    repository.addProfile(profile)

    val updated = profile.copy(name = "Alice Updated", school = "UNIL", profilePicture = "pic2.png")
    repository.updateProfile(updated)

    val fetched = repository.getProfileByUid(uid)
    assertEquals("Alice Updated", fetched?.name)
    assertEquals("UNIL", fetched?.school)
    assertEquals("pic2.png", fetched?.profilePicture)
  }

  @Test(expected = NoSuchElementException::class)
  fun updateProfile_throws_if_not_exists() = runTest {
    val fake = Profile(uid = "nonexistent", name = "Ghost")
    repository.updateProfile(fake)
  }

  @Test
  fun deleteProfile_removes_it() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    val profile = Profile(uid = uid, name = "Alice")
    repository.addProfile(profile)

    assertTrue(repository.isUidRegistered(uid))
    repository.deleteProfile(uid)
    assertFalse(repository.isUidRegistered(uid))
  }

  @Test
  fun isUidRegistered_returns_true_only_if_exists() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    assertFalse(repository.isUidRegistered(uid))

    repository.addProfile(Profile(uid = uid, name = "Alice"))
    assertTrue(repository.isUidRegistered(uid))
  }

  @Test
  fun ensureProfileExists_creates_profile_if_missing() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    val created = repository.ensureProfileExists(uid, defaultPhotoUrl = "default.png")

    assertTrue(created)
    val profile = repository.getProfileByUid(uid)
    assertNotNull(profile)
    assertEquals("default.png", profile!!.profilePicture)
  }

  @Test
  fun ensureProfileExists_returns_false_if_already_exists() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    repository.addProfile(Profile(uid = uid, name = "Existing", profilePicture = "p1.png"))

    val result = repository.ensureProfileExists(uid, defaultPhotoUrl = "new.png")
    assertFalse(result)
  }

  @Test
  fun searchProfilesByNamePrefix_returns_matching_profiles() = runTest {
    val uid = com.android.gatherly.utils.FirebaseEmulator.auth.currentUser!!.uid
    repository.addProfile(Profile(uid = uid, name = "Alice", profilePicture = "p.png"))

    // Another profile with the same user (not allowed by rules, but safe in emulator for test)
    val other = Profile(uid = uid, name = "Alex", profilePicture = "x.png")
    repository.addProfile(other)

    val result = repository.searchProfilesByNamePrefix("Al")
    assertTrue(result.isNotEmpty())
    assertTrue(result.all { it.name.startsWith("Al", ignoreCase = true) })
  }

  @Test
  fun userCannotEditAnotherUserProfile_dueToRules() = runTest {
    // User A creates profile
    val auth = com.android.gatherly.utils.FirebaseEmulator.auth
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(com.android.gatherly.utils.FirebaseEmulator.firestore)
    repoA.addProfile(Profile(uid = userAUid, name = "Alice", profilePicture = "alice.png"))

    // Sign in as another user (User B)
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(com.android.gatherly.utils.FirebaseEmulator.firestore)

    // Attempt to edit Alice's profile — should throw a Firestore permission error
    val unauthorizedUpdate =
        Profile(uid = userAUid, name = "Alice Hacked", profilePicture = "evil.png")
    try {
      repoB.updateProfile(unauthorizedUpdate)
      fail("Expected Firestore PERMISSION_DENIED when editing another user's profile")
    } catch (e: Exception) {
      assertTrue(e.message?.contains("PERMISSION_DENIED") == true)
    }
  }

  @Test
  fun userCanReadOtherUserProfile_whenAuthenticated() = runTest {
    // User A signs in and creates a profile
    val auth = com.android.gatherly.utils.FirebaseEmulator.auth
    val firestore = com.android.gatherly.utils.FirebaseEmulator.firestore

    // Sign in as User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore)

    val alice = Profile(uid = userAUid, name = "Alice", profilePicture = "alice.png")
    repoA.addProfile(alice)

    // Simulate switching to a different user (User B)
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    assertNotEquals(userAUid, userBUid)

    val repoB = ProfileRepositoryFirestore(firestore)

    // User B can read Alice's profile (because rules allow read: if request.auth != null)
    val fetched = repoB.getProfileByUid(userAUid)
    assertNotNull(fetched)
    assertEquals("Alice", fetched!!.name)
  }
}
