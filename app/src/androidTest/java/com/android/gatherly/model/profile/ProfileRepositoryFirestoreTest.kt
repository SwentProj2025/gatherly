package com.android.gatherly.model.profile

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.android.gatherly.model.badge.Rank
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import java.io.OutputStream
import kotlin.io.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

  @Test
  fun deleteUserProfile_removesAllDataAndFreesUsername() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "pic.png")
    repository.registerUsername(uid, "testuser")

    // Add fake profile pic in storage
    val storageRef = com.google.firebase.Firebase.storage.reference.child("profile_pictures/$uid")
    storageRef.putBytes(ByteArray(10)).await()

    // Delete full profile
    repository.deleteUserProfile(uid)

    // Profile document should be gone
    assertFalse(repository.isUidRegistered(uid))

    // Username should be available again
    assertTrue(repository.isUsernameAvailable("testuser"))

    // Picture should no longer exist
    try {
      storageRef.metadata.await()
      fail("Expected picture to be deleted")
    } catch (e: Exception) {
      assertTrue(e.message!!.contains("Object does not exist"))
    }
  }

  @Test
  fun initProfileIfMissing_setsDefaultStatusOffline() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid

    repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")
    val profile = repository.getProfileByUid(uid)

    assertNotNull(profile)
    // New profiles should default to OFFLINE
    assertEquals(ProfileStatus.OFFLINE, profile!!.status)
  }

  @Test
  fun updateStatus_savesCorrectlyInFirestore() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

    // Set status to ONLINE
    repository.updateStatus(uid, ProfileStatus.ONLINE)

    val profile = repository.getProfileByUid(uid)
    assertEquals(ProfileStatus.ONLINE, profile!!.status)

    // Set status back to OFFLINE
    repository.updateStatus(uid, ProfileStatus.OFFLINE)
    val updatedProfile = repository.getProfileByUid(uid)
    assertEquals(ProfileStatus.OFFLINE, updatedProfile!!.status)
  }

  @Test
  fun getProfileByUid_convertsStatusStringToEnum() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

    // Directly update Firestore with a string
    FirebaseEmulator.firestore
        .collection("profiles")
        .document(uid)
        .update("status", "online")
        .await()

    val profile = repository.getProfileByUid(uid)
    assertEquals(ProfileStatus.ONLINE, profile!!.status)

    // Unknown string should default to OFFLINE
    FirebaseEmulator.firestore
        .collection("profiles")
        .document(uid)
        .update("status", "unknown")
        .await()
    val profile2 = repository.getProfileByUid(uid)
    assertEquals(ProfileStatus.OFFLINE, profile2!!.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testCreateEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    // Create a fictional event ID
    val eventId = "BobEventID"

    // UserB create a new event
    repo.createEvent(eventId, userBUid)

    // Verify that the event is in the User B profile's events list
    val profileB = repo.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertTrue(profileB!!.ownedEventIds.contains(eventId))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testDeleteEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    // Create a fictional event ID
    val eventId = "BobEventID"

    // UserB create a new event
    repo.createEvent(eventId, userBUid)

    // Verify that the event is in the User B profile's events list
    val profileB = repo.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertTrue(profileB!!.ownedEventIds.contains(eventId))

    // UserB delete this event
    repo.deleteEvent(eventId, userBUid)

    // Verify that the event is not in the User B profile's events list
    val profile = repo.getProfileByUid(userBUid)
    assertNotNull(profile)
    assertFalse(profile!!.ownedEventIds.contains(eventId))
  }

  @Test
  fun testParticipateEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    // Create a fictional event ID
    val eventId = "AliceEventID"

    // UserB create a new event
    repo.createEvent(eventId, userAUid)

    // Verify that the event is in the User B profile's events list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertTrue(profileA!!.ownedEventIds.contains(eventId))

    auth.signOut()

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    repo.participateEvent(eventId, userBUid)

    // Verify that the event is in the User B profile's events list
    val profileB = repo.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertTrue(profileB!!.participatingEventIds.contains(eventId))
  }

  @Test
  fun testUnregisterEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    // Create a fictional event ID
    val eventId = "AliceEventID"

    // UserB create a new event
    repo.createEvent(eventId, userAUid)

    // Verify that the event is in the User B profile's events list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertTrue(profileA!!.ownedEventIds.contains(eventId))

    auth.signOut()

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    repo.participateEvent(eventId, userBUid)

    // Verify that the event is in the User B profile's events list
    val profileB = repo.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertTrue(profileB!!.participatingEventIds.contains(eventId))

    repo.unregisterEvent(eventId, userBUid)

    // Verify that the event is not in te User B profile's events list
    val profile = repo.getProfileByUid(userBUid)
    assertNotNull(profile)
    assertFalse(profile!!.participatingEventIds.contains(eventId))
  }

  @Test
  fun testAllParticipateEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    auth.signOut()

    // User C
    auth.signInAnonymously().await()
    val userCUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userCUid, "charlie.png")
    auth.signOut()

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    // Create a fictional event ID
    val eventId = "AliceEventID"

    // UserB create a new event
    repo.createEvent(eventId, userAUid)

    // Verify that the event is in the User B profile's events list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertTrue(profileA!!.ownedEventIds.contains(eventId))

    val participants = listOf(userAUid, userBUid, userCUid)

    repo.allParticipateEvent(eventId, participants)

    val updatedProfileA = repo.getProfileByUid(userAUid)
    val updatedProfileB = repo.getProfileByUid(userBUid)
    val updatedProfileC = repo.getProfileByUid(userCUid)

    assertNotNull(updatedProfileA)
    assertNotNull(updatedProfileB)
    assertNotNull(updatedProfileC)
    assertTrue(updatedProfileA!!.participatingEventIds.contains(eventId))
    assertTrue(updatedProfileB!!.participatingEventIds.contains(eventId))
    assertTrue(updatedProfileC!!.participatingEventIds.contains(eventId))
  }

  @Test
  fun testAllUnregisterEvent() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    val profileB = repo.getProfileByUid(userBUid)
    auth.signOut()

    // User C
    auth.signInAnonymously().await()
    val userCUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userCUid, "charlie.png")
    val profileC = repo.getProfileByUid(userCUid)
    auth.signOut()

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    // Create a fictional event ID
    val eventId = "AliceEventID"

    // UserB create a new event
    repo.createEvent(eventId, userAUid)

    // Verify that the event is in the User B profile's events list
    val profileA = repo.getProfileByUid(userAUid)
    assertNotNull(profileA)
    assertTrue(profileA!!.ownedEventIds.contains(eventId))

    val participants = listOf(userAUid, userBUid, userCUid)

    repo.allParticipateEvent(eventId, participants)

    val updatedProfileA = repo.getProfileByUid(userAUid)
    val updatedProfileB = repo.getProfileByUid(userBUid)
    val updatedProfileC = repo.getProfileByUid(userCUid)

    assertNotNull(updatedProfileA)
    assertNotNull(updatedProfileB)
    assertNotNull(updatedProfileC)
    assertTrue(updatedProfileA!!.participatingEventIds.contains(eventId))
    assertTrue(updatedProfileB!!.participatingEventIds.contains(eventId))
    assertTrue(updatedProfileC!!.participatingEventIds.contains(eventId))

    repo.allUnregisterEvent(eventId, participants)

    val updated2ProfileA = repo.getProfileByUid(userAUid)
    val updated2ProfileB = repo.getProfileByUid(userBUid)
    val updated2ProfileC = repo.getProfileByUid(userCUid)

    assertNotNull(updated2ProfileA)
    assertNotNull(updated2ProfileB)
    assertNotNull(updated2ProfileC)
    assertFalse(updated2ProfileA!!.participatingEventIds.contains(eventId))
    assertFalse(updated2ProfileB!!.participatingEventIds.contains(eventId))
    assertFalse(updated2ProfileC!!.participatingEventIds.contains(eventId))
  }

  @Test
  fun testSetupBadgesCorrectly() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    val profileB: Profile? = repo.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertEquals(profileB!!.badges.createEvent, Rank.BLANK)
    assertEquals(profileB.badges.participateEvent, Rank.BLANK)
    assertEquals(profileB.badges.focusSessionPoint, Rank.BLANK)
    assertEquals(profileB.badges.addFriends, Rank.BLANK)
    assertEquals(profileB.badges.completedTodos, Rank.BLANK)
    assertEquals(profileB.badges.createdTodos, Rank.BLANK)
    auth.signOut()
  }

  @Test
  fun testUpdateBadgeCorrectlyAddFriends() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "bob.png")

    val massiveFriendCount = 20
    val fakeFriendUids = List(massiveFriendCount) { "fakeUid$it" }

    val initialProfile = repository.getProfileByUid(uid)!!
    val profileWithFakeFriends =
        initialProfile.copy(
            friendUids = fakeFriendUids,
        )
    repository.updateProfile(profileWithFakeFriends)

    val profileToUpdate = repository.getProfileByUid(uid)!!

    repository.updateBadges(profileToUpdate)
    val updatedProfile = repository.getProfileByUid(uid)!!

    assertEquals(20, updatedProfile.friendUids.size)
    assertEquals(Rank.LEGEND, updatedProfile.badges.addFriends)
  }

  @Test
  fun testUpdateBadgeCorrectlyFocusSessionPoints() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "bob.png")

    val initialProfile = repository.getProfileByUid(uid)!!
    val profileWithFakeFocusSession =
        initialProfile.copy(
            focusSessionIds = listOf("fakeFocusSessionIds"),
        )

    repository.updateProfile(profileWithFakeFocusSession)

    val profileToUpdate = repository.getProfileByUid(uid)!!

    repository.updateBadges(profileToUpdate)
    val updatedProfile = repository.getProfileByUid(uid)!!

    assertEquals(1, updatedProfile.focusSessionIds.size)
    assertEquals(Rank.STARTING, updatedProfile.badges.focusSessionPoint)
  }

  @Test
  fun testUpdateBadgeCorrectlyCreatedEventParticipatingEvent() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "bob.png")

    val createdEventCount = 3
    val fakeEventUids = List(createdEventCount) { "fakeUid$it" }

    val participatingEventCount = 11
    val fakeParticipatingEventsUids = List(participatingEventCount) { "participatingFakeUid$it" }

    val initialProfile = repository.getProfileByUid(uid)!!
    val profileWithFakeEvents =
        initialProfile.copy(
            ownedEventIds = fakeEventUids, participatingEventIds = fakeParticipatingEventsUids)
    repository.updateProfile(profileWithFakeEvents)

    val profileToUpdate = repository.getProfileByUid(uid)!!

    repository.updateBadges(profileToUpdate)
    val updatedProfile = repository.getProfileByUid(uid)!!

    assertEquals(3, updatedProfile.ownedEventIds.size)
    assertEquals(11, updatedProfile.participatingEventIds.size)
    assertEquals(Rank.BRONZE, updatedProfile.badges.createEvent)
    assertEquals(Rank.DIAMOND, updatedProfile.badges.participateEvent)
  }

  @Test
  fun testUpdateBadgeCorrectlyCreatedTodosCompletedTodos() = runTest {
    val uid = FirebaseEmulator.auth.currentUser!!.uid
    repository.initProfileIfMissing(uid, "tester.png")

    val db = FirebaseEmulator.firestore

    val todosSnap = db.collection("users").document(uid).collection("todos").get().await()
    for (doc in todosSnap.documents) {
      db.collection("users").document(uid).collection("todos").document(doc.id).delete().await()
    }

    writeTodo(db, uid, "todo_completed_1", ToDoStatus.ENDED)
    writeTodo(db, uid, "todo_completed_2", ToDoStatus.ENDED)
    writeTodo(db, uid, "todo_completed_3", ToDoStatus.ENDED)

    writeTodo(db, uid, "todo_ongoing_4", ToDoStatus.ONGOING)
    writeTodo(db, uid, "todo_ongoing_5", ToDoStatus.ONGOING)

    val profileToUpdate = repository.getProfileByUid(uid)!!
    repository.updateBadges(profileToUpdate, null, null)

    val updatedProfile = repository.getProfileByUid(uid)!!

    assertEquals(Rank.GOLD, updatedProfile.badges.createdTodos)
    assertEquals(Rank.BRONZE, updatedProfile.badges.completedTodos)
  }

  @Test
  fun sendFriendRequest_addsPendingEntriesToBothUsers() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    auth.signOut()

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    // A → B friend request
    repo.sendFriendRequest(userAUid, userBUid)

    // Wait for Firestore propagation
    var profileA: Profile? = null
    var profileB: Profile? = null
    withTimeout(TIMEOUT) {
      while (true) {
        profileA = repo.getProfileByUid(userAUid)
        profileB = repo.getProfileByUid(userBUid)
        if (profileA!!.pendingFriendsOutgoing.contains(userBUid) &&
            profileB!!.pendingFriendsIncoming.contains(userAUid))
            break
        delay(DELAY)
      }
    }

    assertTrue(profileA!!.pendingFriendsOutgoing.contains(userBUid))
    assertTrue(profileB!!.pendingFriendsIncoming.contains(userAUid))
  }

  @Test
  fun sendFriendRequest_isIdempotent_noDuplicates() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    auth.signOut()

    // User B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    // Call 3 times
    repeat(3) { repo.sendFriendRequest(userAUid, userBUid) }

    var profileA: Profile? = null
    var profileB: Profile? = null
    withTimeout(TIMEOUT) {
      while (true) {
        profileA = repo.getProfileByUid(userAUid)
        profileB = repo.getProfileByUid(userBUid)
        if (profileA!!.pendingFriendsOutgoing.contains(userBUid) &&
            profileB!!.pendingFriendsIncoming.contains(userAUid))
            break
        delay(DELAY)
      }
    }

    assertEquals(1, profileA!!.pendingFriendsOutgoing.count { it == userBUid })
    assertEquals(1, profileB!!.pendingFriendsIncoming.count { it == userAUid })
  }

  @Test
  fun acceptFriendRequest_movesPendingToFriends() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    auth.signOut()

    // User B (recipient)
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    repo.sendFriendRequest(userAUid, userBUid)

    // Build notification
    val notif =
        Notification(
            id = "notif1",
            type = com.android.gatherly.model.notification.NotificationType.FRIEND_REQUEST,
            emissionTime = com.google.firebase.Timestamp.now(),
            senderId = userAUid,
            recipientId = userBUid,
            wasRead = false,
            relatedEntityId = null)

    // B accepts A
    repo.acceptFriendRequest(notif)

    var profileA: Profile? = null
    var profileB: Profile? = null

    withTimeout(TIMEOUT) {
      while (true) {
        profileA = repo.getProfileByUid(userAUid)
        profileB = repo.getProfileByUid(userBUid)

        val bothFriends =
            profileA!!.friendUids.contains(userBUid) && profileB!!.friendUids.contains(userAUid)
        val pendingCleared =
            !profileA!!.pendingFriendsOutgoing.contains(userBUid) &&
                !profileB!!.pendingFriendsIncoming.contains(userAUid)

        if (bothFriends && pendingCleared) break
        delay(DELAY)
      }
    }

    assertTrue(profileA!!.friendUids.contains(userBUid))
    assertTrue(profileB!!.friendUids.contains(userAUid))
    assertFalse(profileA!!.pendingFriendsOutgoing.contains(userBUid))
    assertFalse(profileB!!.pendingFriendsIncoming.contains(userAUid))
  }

  @Test
  fun rejectFriendRequest_clearsPendingButNoFriendship() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    auth.signOut()

    // B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")

    repo.sendFriendRequest(userAUid, userBUid)

    val notif =
        Notification(
            id = "notif2",
            type = NotificationType.FRIEND_REQUEST,
            emissionTime = Timestamp.now(),
            senderId = userAUid,
            recipientId = userBUid,
            wasRead = false,
            relatedEntityId = null)

    repo.rejectFriendRequest(notif)

    var profileA: Profile? = null
    var profileB: Profile? = null

    withTimeout(TIMEOUT) {
      while (true) {
        profileA = repo.getProfileByUid(userAUid)
        profileB = repo.getProfileByUid(userBUid)

        val noPending =
            !profileA!!.pendingFriendsOutgoing.contains(userBUid) &&
                !profileB!!.pendingFriendsIncoming.contains(userAUid)
        if (noPending) break

        delay(DELAY)
      }
    }

    assertFalse(profileA!!.pendingFriendsOutgoing.contains(userBUid))
    assertFalse(profileB!!.pendingFriendsIncoming.contains(userAUid))

    assertFalse(profileA!!.friendUids.contains(userBUid))
    assertFalse(profileB!!.friendUids.contains(userAUid))
  }

  @Test
  fun deleteFriend_isSymmetric_removesFromBothUsers() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")
    repo.registerUsername(userAUid, "alice")
    auth.signOut()

    // B
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userBUid, "bob.png")
    repo.registerUsername(userBUid, "bob")

    // A adds B
    repo.addFriend("bob", userAUid)

    // wait until Firestore has updated
    withTimeout(TIMEOUT) {
      while (true) {
        val a = repo.getProfileByUid(userAUid)!!
        if (a.friendUids.contains(userBUid)) break
        delay(DELAY)
      }
    }

    // Now delete friendship
    repo.deleteFriend("bob", userAUid)

    withTimeout(TIMEOUT) {
      while (true) {
        val a = repo.getProfileByUid(userAUid)!!
        val b = repo.getProfileByUid(userBUid)!!
        if (!a.friendUids.contains(userBUid) && !b.friendUids.contains(userAUid)) break
        delay(DELAY)
      }
    }

    val profileA = repo.getProfileByUid(userAUid)!!
    val profileB = repo.getProfileByUid(userBUid)!!

    assertFalse(profileA.friendUids.contains(userBUid))
    assertFalse(profileB.friendUids.contains(userAUid))
  }

  /** Helper function to write a ToDo document in Firestore for testing. */
  private fun writeTodo(
      db: FirebaseFirestore,
      userId: String,
      todoId: String,
      status: ToDoStatus,
      ownerId: String = userId
  ) {
    val todoData =
        mapOf(
            "uid" to todoId,
            "name" to "Test ToDo $todoId",
            "description" to "Description",
            "assigneeName" to "Assignee",
            "dueDate" to com.google.firebase.Timestamp.now(),
            "ownerId" to ownerId,
            "status" to status.name)

    db.collection("users").document(userId).collection("todos").document(todoId).set(todoData)
  }

  @Test
  fun deleteUserProfile_removesUserFromAllGroups() = runTest {
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val auth = FirebaseEmulator.auth

    // USER B (normal member)
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore, storage)
    repoB.initProfileIfMissing(userBUid, "picB.png")
    auth.signOut()

    // USER A (to be deleted)
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore, storage)
    repoA.initProfileIfMissing(userAUid, "picA.png")

    val group1 = firestore.collection("groups").document()
    group1
        .set(
            mapOf(
                "name" to "Group 1",
                "creatorId" to userAUid,
                "adminIds" to listOf(userAUid),
                "memberIds" to listOf(userAUid, userBUid)))
        .await()

    val group2 = firestore.collection("groups").document()
    group2
        .set(
            mapOf(
                "name" to "Group 2",
                "creatorId" to userAUid,
                "adminIds" to listOf(userAUid),
                "memberIds" to listOf(userAUid)))
        .await()

    // Now delete user A fully
    repoA.deleteUserProfile(userAUid)

    // Reload groups
    val g1 = group1.get().await().data!!
    val g1Members = g1["memberIds"] as List<*>
    val g1Admins = g1["adminIds"] as List<*>

    val g2 = group2.get().await().data!!
    val g2Members = g2["memberIds"] as List<*>
    val g2Admins = g2["adminIds"] as List<*>

    // Assertions
    assertFalse(g1Members.contains(userAUid))
    assertTrue(g1Admins.isEmpty()) // A was the only admin

    assertFalse(g2Members.contains(userAUid))
    assertTrue(g2Admins.isEmpty()) // A removed from adminIds
  }

  @Test
  fun deleteUserProfile_deletesAllUserOwnedDocuments() = runTest {
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val auth = FirebaseEmulator.auth
    val repo = ProfileRepositoryFirestore(firestore, storage)

    auth.signInAnonymously().await()
    val uid = auth.currentUser!!.uid
    repo.initProfileIfMissing(uid, "pic.png")

    // Add a todo
    val todoRef = firestore.collection("users").document(uid).collection("todos").document()
    todoRef.set(mapOf("text" to "test todo")).await()

    // Add an event
    val eventRef = firestore.collection("events").document()
    eventRef.set(mapOf("creatorId" to uid, "name" to "Test Event")).await()

    // Add a focus session
    val focusRef = firestore.collection("focusSessions").document()
    focusRef.set(mapOf("creatorId" to uid, "duration" to 25)).await()

    // Now delete user
    repo.deleteUserProfile(uid)

    // All should be gone
    assertFalse(todoRef.get().await().exists())
    assertFalse(eventRef.get().await().exists())
    assertFalse(focusRef.get().await().exists())
  }

  @Test
  fun deleteUserProfile_doesNotAffectOtherUsersData() = runTest {
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val auth = FirebaseEmulator.auth

    // USER B (must create their own profile)
    auth.signOut()
    auth.signInAnonymously().await()
    val userBUid = auth.currentUser!!.uid
    val repoB = ProfileRepositoryFirestore(firestore, storage)
    repoB.initProfileIfMissing(userBUid, "b.png")
    repoB.registerUsername(userBUid, "bob")

    auth.signOut()
    // USER A (to be deleted)
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    val repoA = ProfileRepositoryFirestore(firestore, storage)
    repoA.initProfileIfMissing(userAUid, "a.png")
    repoA.registerUsername(userAUid, "alice")

    repoA.deleteUserProfile(userAUid)

    // USER B's data must remain
    val profileB = repoB.getProfileByUid(userBUid)
    assertNotNull(profileB)
    assertEquals("bob", profileB!!.username)
  }
}
