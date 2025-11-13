package com.android.gatherly.model.profile

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import java.io.OutputStream
import kotlin.io.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

private const val TIMEOUT = 3000L
private const val DELAY = 100L

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
    val storage = FirebaseEmulator.storage

    // USER A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore, storage)
    repoA.initProfileIfMissing(userAUid, "defaultA.png")
    repoA.updateProfile(Profile(uid = userAUid, name = "Alice", profilePicture = "a.png"))

    // USER B
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore, storage)
    repoB.initProfileIfMissing(userBUid, "defaultB.png")
    repoB.updateProfile(Profile(uid = userBUid, name = "Alex", profilePicture = "b.png"))

    // SEARCH (performed by User B)
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
    val snap = FirebaseEmulator.firestore.collection("profiles").document(uid).get().await()
    delay(500)

    val success = repository.registerUsername(uid, "alice")
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
    val storage = FirebaseEmulator.storage

    // User A creates a profile
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore, storage)
    repoA.initProfileIfMissing(userAUid, "alice.png")

    // Switch to User B
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore, storage)

    // Attempt to edit Alice's profile should fail
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
    val storage = FirebaseEmulator.storage

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore, storage)
    repoA.initProfileIfMissing(userAUid, "alice.png")

    // User B
    auth.signOut()
    auth.signInAnonymously().await()
    val repoB = ProfileRepositoryFirestore(firestore, storage)

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

  @Test
  fun testGetListNoFriends() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    val userBUsername = "bob"
    repo.registerUsername(userBUid, userBUsername)
    auth.signOut()

    // User C
    auth.signInAnonymously().await()
    val userCUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userCUid, "charlie.png")
    val userCUsername = "charlie"
    repo.registerUsername(userCUid, userCUsername)
    auth.signOut()

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    val userAUsername = "alice"
    repo.registerUsername(userAUid, userAUsername)

    // User A adds User B as a friend
    repo.addFriend(userBUsername, userAUid)

    var updatedProfileA: Profile? = repo.getProfileByUid(userAUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (updatedProfileA?.friendUids?.contains(userBUid) != true) {
          updatedProfileA = repo.getProfileByUid(userAUid)
          delay(DELAY)
        }
      }
    }

    // Check the non-friends list for User A
    val noFriendsList = repo.getListNoFriends(userAUid)
    assertEquals(listOf("charlie"), noFriendsList)

    // Check the non-friends list for User C
    val noFriendsListC = repo.getListNoFriends(userCUid)
    assertTrue(
        noFriendsListC.size == 2 &&
            noFriendsListC.contains("alice") &&
            noFriendsListC.contains("bob"))
  }

  @Test
  fun testAddFriend() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    val userBUsername = "bob"
    repo.registerUsername(userBUid, userBUsername)
    auth.signOut()

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    val userAUsername = "alice"
    repo.registerUsername(userAUid, userAUsername)

    // User A adds User B as a friend
    repo.addFriend(userBUsername, userAUid)

    var updatedProfileA: Profile? = repo.getProfileByUid(userAUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (updatedProfileA?.friendUids?.contains(userBUid) != true) {
          updatedProfileA = repo.getProfileByUid(userAUid)
          delay(DELAY)
        }
      }
    }

    // Verify that User B is in User A's friend list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertTrue(profileA!!.friendUids.contains(userBUid))
  }

  @Test
  fun deleteFriend_removesUidFromFriendList() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    val userBUsername = "bob"
    repo.registerUsername(userBUid, userBUsername)
    auth.signOut()

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    val userAUsername = "alice"
    repo.registerUsername(userAUid, userAUsername)

    // User A adds User B as a friend
    repo.addFriend(userBUsername, userAUid)

    var updatedProfileA: Profile? = null
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (updatedProfileA?.friendUids?.contains(userBUid) != true) {
          updatedProfileA = repo.getProfileByUid(userAUid)
          delay(DELAY)
        }
      }
    }

    // Check that User B is in User A's friend list
    val profile = repo.getProfileByUid(userAUid)
    assertNotNull(profile)
    assertTrue(profile!!.friendUids.contains(userBUid))

    // User A deletes User B from friends
    repo.deleteFriend(userBUsername, userAUid)

    updatedProfileA = repo.getProfileByUid(userAUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (updatedProfileA?.friendUids?.contains(userBUid) == true) {
          updatedProfileA = repo.getProfileByUid(userAUid)
          delay(DELAY)
        }
      }
    }

    // Verify that User B is no longer in User A's friend list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertFalse(profileA!!.friendUids.contains(userBUid))
  }

  @Test
  fun updateProfilePic_overwritesExistingFile() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    withTimeout(TIMEOUT) { while (FirebaseEmulator.auth.currentUser == null) delay(DELAY) }
    repository.initProfileIfMissing(uid, "pic.png")

    val file1 = kotlin.io.path.createTempFile("first").toFile()
    val file2 = kotlin.io.path.createTempFile("second").toFile()

    try {
      val bytes1 = ByteArray(20) { 1 }
      file1.writeBytes(bytes1)
      val uri1 = Uri.fromFile(file1)

      repository.updateProfilePic(uid, uri1)
      val storageRef = FirebaseEmulator.storage.reference.child("profile_pictures/$uid")
      val metadata1 = storageRef.metadata.await()
      val downloaded1 = storageRef.getBytes(1024 * 1024).await()

      val bytes2 = ByteArray(30) { 2 }
      file2.writeBytes(bytes2)
      val uri2 = Uri.fromFile(file2)

      repository.updateProfilePic(uid, uri2)
      val metadata2 = storageRef.metadata.await()
      val downloaded2 = storageRef.getBytes(1024 * 1024).await()

      assertTrue(metadata2.updatedTimeMillis >= metadata1.updatedTimeMillis)
      assertFalse("Expected file content to change", downloaded1.contentEquals(downloaded2))
    } finally {
      file1.delete()
      file2.delete()
    }
  }

  @Test
  fun updateProfilePic_withFileUri_uploadsSuccessfully() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "")

    val tmpFile = kotlin.io.path.createTempFile("test_image").toFile()

    try {
      val expectedBytes = ByteArray(20) { 0x42 }
      tmpFile.writeBytes(expectedBytes)
      val fileUri = Uri.fromFile(tmpFile)

      val downloadUrl = repository.updateProfilePic(uid, fileUri)
      assertTrue(downloadUrl.startsWith("http"))

      val storageRef = FirebaseEmulator.storage.reference.child("profile_pictures/$uid")
      val metadata = storageRef.metadata.await()
      assertTrue(metadata.sizeBytes > 0)

      val actualBytes = storageRef.getBytes(1024 * 1024).await()
      assertArrayEquals(expectedBytes, actualBytes.take(expectedBytes.size).toByteArray())
    } finally {
      tmpFile.delete()
    }
  }

  // This code was partly generated by chat gpt:
  @Test
  fun updateProfilePic_contentUri_uploadsSuccessfully() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "")

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Create bytes we expect
    val expectedBytes = ByteArray(20) { 0x33 }

    // Create a content:// uri via MediaStore
    val values =
        ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, "test_image_${System.currentTimeMillis()}.bin")
          put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
        }

    val contentUri =
        context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)!!

    // Write our bytes into that content:// uri
    context.contentResolver.openOutputStream(contentUri)?.use { output: OutputStream ->
      output.write(expectedBytes)
      output.flush()
    }

    // Call the method under test, this will hit the content:// branch
    val downloadUrl = repository.updateProfilePic(uid, contentUri)
    assertTrue(downloadUrl.startsWith("http"))

    // Verify bytes reached Firebase emulator
    val uploadedBytes =
        FirebaseEmulator.storage.reference
            .child("profile_pictures/$uid")
            .getBytes(1024 * 1024)
            .await()

    assertArrayEquals(expectedBytes, uploadedBytes.take(expectedBytes.size).toByteArray())
  }

  /** Test: Verifies that the friendUsernames list and nonFriendUsernames list are correctly set. */
  @Test
  fun test_getFriendsAndNonFriendsUsernames_success() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    repo.registerUsername(userBUid, "bob")
    auth.signOut()

    //  User C
    auth.signInAnonymously().await()
    val userCUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userCUid, "charlie.png")
    repo.registerUsername(userCUid, "charlie")
    auth.signOut()

    // User A current user
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    repo.registerUsername(userAUid, "alice")

    repo.addFriend("bob", userAUid)

    var updatedProfileA: Profile? = repo.getProfileByUid(userAUid)
    withContext(Dispatchers.Default.limitedParallelism(1)) {
      withTimeout(TIMEOUT) {
        while (updatedProfileA?.friendUids?.contains(userBUid) != true) {
          updatedProfileA = repo.getProfileByUid(userAUid)
          delay(DELAY)
        }
      }
    }

    val friendsResult = repo.getFriendsAndNonFriendsUsernames(userAUid)

    assertEquals(1, friendsResult.friendUsernames.size)
    assertTrue(friendsResult.friendUsernames.contains("bob"))

    assertEquals(1, friendsResult.nonFriendUsernames.size)
    assertTrue(friendsResult.nonFriendUsernames.contains("charlie"))

    assertFalse(friendsResult.friendUsernames.contains("alice"))
    assertFalse(friendsResult.nonFriendUsernames.contains("alice"))
  }

  /** Test : */
  @Test(expected = NoSuchElementException::class)
  fun test_getFriendsAndNonFriendsUsernames_throwsIfProfileMissing() = runTest {
    repository.getFriendsAndNonFriendsUsernames("non_existent_uid")
  }
}
