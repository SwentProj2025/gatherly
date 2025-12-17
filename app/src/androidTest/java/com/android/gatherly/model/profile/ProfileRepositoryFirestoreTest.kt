package com.android.gatherly.model.profile

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.android.gatherly.model.event.Event
import com.android.gatherly.model.event.EventStatus
import com.android.gatherly.model.event.EventsRepositoryFirestore
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.utils.FirebaseEmulator
import com.android.gatherly.utils.FirestoreGatherlyProfileTest
import com.android.gatherly.utils.createEvent
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.NoSuchElementException
import kotlin.io.use
import kotlin.time.Duration.Companion.seconds
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
  fun initProfileIfMissing_createsProfileWhenAbsent() =
      runTest(timeout = 120.seconds) {
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
  fun initProfileIfMissing_returnsFalseWhenAlreadyExists() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid

        repository.initProfileIfMissing(uid, "photo1.png")
        val result = repository.initProfileIfMissing(uid, "photo2.png")

        assertFalse(result)
        val profile = repository.getProfileByUid(uid)
        assertEquals("photo1.png", profile!!.profilePicture) // unchanged
      }

  @Test
  fun updateProfile_updatesExistingProfile() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "pic1.png")

        val updated =
            Profile(
                uid = uid,
                name = "Alice",
                school = "EPFL",
                profilePicture = "updated.png",
                userStatusSource = UserStatusSource.MANUAL)
        repository.updateProfile(updated)

        val fetched = repository.getProfileByUid(uid)
        assertEquals("Alice", fetched!!.name)
        assertEquals("EPFL", fetched.school)
        assertEquals("updated.png", fetched.profilePicture)
        assertEquals(UserStatusSource.MANUAL, fetched.userStatusSource)
      }

  @Test(expected = NoSuchElementException::class)
  fun updateProfile_throwsIfNotExists() =
      runTest(timeout = 120.seconds) {
        val fake = Profile(uid = "nonexistent", name = "Ghost")
        repository.updateProfile(fake)
      }

  @Test
  fun deleteProfile_removesItFromDatabase() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "p.png")

        assertTrue(repository.isUidRegistered(uid))
        repository.deleteProfile(uid)
        assertFalse(repository.isUidRegistered(uid))
      }

  @Test
  fun isUidRegistered_returnsTrueOnlyIfExists() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        assertFalse(repository.isUidRegistered(uid))

        repository.initProfileIfMissing(uid, "pic.png")
        assertTrue(repository.isUidRegistered(uid))
      }

  @Test
  fun searchProfilesByNamePrefix_returnsMatchingProfiles() =
      runTest(timeout = 120.seconds) {
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
  fun registerUsername_failsIfProfileDoesNotExist() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        try {
          repository.registerUsername(uid, "newuser")
          fail("Expected IllegalStateException because profile does not exist yet")
        } catch (e: IllegalStateException) {
          assertTrue(e.message!!.contains("profile creation"))
        }
      }

  @Test
  fun registerUsername_updatesProfileWhenProfileExists() =
      runTest(timeout = 120.seconds) {
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
  fun updateUsername_replacesOldUsername() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "pic.png")
        repository.registerUsername(uid, "oldname")

        val success = repository.updateUsername(uid, "oldname", "newname")
        assertTrue(success)

        val updatedProfile = repository.getProfileByUid(uid)
        assertEquals("newname", updatedProfile!!.username)
      }

  @Test
  fun getProfileByUsername_returnsCorrectProfile() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "photo.png")
        repository.registerUsername(uid, "charlie")

        val fetched = repository.getProfileByUsername("charlie")
        assertNotNull(fetched)
        assertEquals(uid, fetched!!.uid)
      }

  @Test
  fun userCannotEditAnotherUserProfile_dueToRules() =
      runTest(timeout = 120.seconds) {
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
        val unauthorized =
            Profile(uid = userAUid, name = "Hacked Alice", profilePicture = "evil.png")
        try {
          repoB.updateProfile(unauthorized)
          fail("Expected Firestore PERMISSION_DENIED when editing another user's profile")
        } catch (e: Exception) {
          assertTrue(e.message?.contains("PERMISSION_DENIED") == true)
        }
      }

  @Test
  fun userCanReadOtherUserProfile_whenAuthenticated() =
      runTest(timeout = 120.seconds) {
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
  fun isUsernameAvailable_behavesCorrectly() =
      runTest(timeout = 120.seconds) {
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
  fun testGetListNoFriends() =
      runTest(timeout = 120.seconds) {
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
  fun testAddFriend() =
      runTest(timeout = 120.seconds) {
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
  fun deleteFriend_removesUidFromFriendList() =
      runTest(timeout = 120.seconds) {
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
  fun updateProfilePic_overwritesExistingFile() =
      runTest(timeout = 120.seconds) {
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
  fun updateProfilePic_withFileUri_uploadsSuccessfully() =
      runTest(timeout = 120.seconds) {
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
  fun updateProfilePic_contentUri_uploadsSuccessfully() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "")

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Create bytes we expect
        val expectedBytes = ByteArray(20) { 0x33 }

        // Create a content:// uri via MediaStore
        val values =
            ContentValues().apply {
              put(
                  MediaStore.MediaColumns.DISPLAY_NAME,
                  "test_image_${System.currentTimeMillis()}.bin")
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
  fun test_getFriendsAndNonFriendsUsernames_success() =
      runTest(timeout = 120.seconds) {
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
  fun test_getFriendsAndNonFriendsUsernames_throwsIfProfileMissing() =
      runTest(timeout = 120.seconds) {
        repository.getFriendsAndNonFriendsUsernames("non_existent_uid")
      }

  @Test
  fun deleteProfile_removesProfileUsernameAndPicture() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "pic.png")
        repository.registerUsername(uid, "testuser")

        // Add fake profile pic in storage
        val storageRef =
            com.google.firebase.Firebase.storage.reference.child("profile_pictures/$uid")
        storageRef.putBytes(ByteArray(10)).await()

        // Delete full profile
        repository.deleteProfile(uid)

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
  fun initProfileIfMissing_correctlySetsStatus() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid

        repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")
        val profile = repository.getProfileByUid(uid)

        assertNotNull(profile)
        // New profiles should default to OFFLINE
        assertEquals(ProfileStatus.OFFLINE, profile!!.status)
        // and status source to automatic
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)
      }

  @Test
  fun updateStatus_savesCorrectlyInFirestore() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, defaultPhotoUrl = "default.png")

        // Set status to ONLINE
        repository.updateStatus(uid, ProfileStatus.ONLINE, source = UserStatusSource.MANUAL)

        val profile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.ONLINE, profile!!.status)
        assertEquals(UserStatusSource.MANUAL, profile.userStatusSource)

        // Set status back to OFFLINE
        repository.updateStatus(uid, ProfileStatus.OFFLINE)
        val updatedProfile = repository.getProfileByUid(uid)
        assertEquals(ProfileStatus.OFFLINE, updatedProfile!!.status)
      }

  @Test
  fun getProfileByUid_convertsStatusStringToEnum() =
      runTest(timeout = 120.seconds) {
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
        // verifies if correct userStatusSource value is fetched with getProfileByUid
        assertEquals(UserStatusSource.AUTOMATIC, profile.userStatusSource)

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
  fun testCreateEvent() =
      runTest(timeout = 120.seconds) {
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
  fun testDeleteEvent() =
      runTest(timeout = 120.seconds) {
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
  fun testParticipateEvent() =
      runTest(timeout = 120.seconds) {
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
  fun testUnregisterEvent() =
      runTest(timeout = 120.seconds) {
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
  fun testAllParticipateEvent() =
      runTest(timeout = 120.seconds) {
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
  fun testAllUnregisterEvent() =
      runTest(timeout = 120.seconds) {
        val auth = FirebaseEmulator.auth
        val firestore = FirebaseEmulator.firestore
        val storage = FirebaseEmulator.storage
        val repo = ProfileRepositoryFirestore(firestore, storage)
        val eventRepo = EventsRepositoryFirestore(firestore)

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
        val event =
            Event(
                id = eventId,
                title = "Team Meeting",
                description = "Weekly sync",
                creatorName = "Alice",
                location = null,
                date = Timestamp(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))),
                startTime =
                    Timestamp(
                        SimpleDateFormat("HH:mm").parse("12:00")
                            ?: throw NoSuchElementException("no date ")),
                endTime =
                    Timestamp(
                        SimpleDateFormat("HH:mm").parse("23:00")
                            ?: throw NoSuchElementException("no date ")),
                creatorId = userAUid,
                participants = listOf(userAUid, userBUid, userCUid),
                status = EventStatus.UPCOMING)

        eventRepo.addEvent(event)

        val pointsRepository = PointsLocalRepository()

        // UserB create a new event
        createEvent(
            eventRepo,
            repo,
            pointsRepository,
            event,
            userAUid,
            emptyList(),
            notificationsRepository = NotificationsLocalRepository())

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
  fun testAddBadgesCorrectlyCreatedEvent() =
      runTest(timeout = 120.seconds) {
        val auth = FirebaseEmulator.auth
        val firestore = FirebaseEmulator.firestore
        val storage = FirebaseEmulator.storage
        val repo = ProfileRepositoryFirestore(firestore, storage)
        val eventRepo = EventsRepositoryFirestore(firestore)

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
        auth.signOut()

        // User A
        auth.signInAnonymously().await()
        val userAUid = auth.currentUser!!.uid
        repo.initProfileIfMissing(userAUid, "alice.png")

        // Create a fictional event ID
        val eventId = "AliceEventID"
        val event =
            Event(
                id = eventId,
                title = "Team Meeting",
                description = "Weekly sync",
                creatorName = "Alice",
                location = null,
                date = Timestamp(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))),
                startTime =
                    Timestamp(
                        SimpleDateFormat("HH:mm").parse("12:00")
                            ?: throw NoSuchElementException("no date ")),
                endTime =
                    Timestamp(
                        SimpleDateFormat("HH:mm").parse("23:00")
                            ?: throw NoSuchElementException("no date ")),
                creatorId = userAUid,
                participants = listOf(userAUid, userBUid, userCUid),
                status = EventStatus.UPCOMING)

        eventRepo.addEvent(event)

        val pointsRepository = PointsLocalRepository()

        createEvent(
            eventRepo,
            repo,
            pointsRepository,
            event,
            userAUid,
            emptyList(),
            notificationsRepository = NotificationsLocalRepository())

        assertTrue(
            repository.getProfileByUid(userAUid)?.badgeIds?.contains("starting_EventsCreated") ==
                true)
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
  fun deleteProfile_doesNotAffectOtherUsersData() =
      runTest(timeout = 120.seconds) {
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

        repoA.deleteProfile(userAUid)

        // USER B's data must remain
        val profileB = repoB.getProfileByUid(userBUid)
        assertNotNull(profileB)
        assertEquals("bob", profileB!!.username)
      }

  /** Check that adding focus points adds focus points to global points and weekly points */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun addFocusPointsWorks() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "pic.png")

        val profileBefore = repository.getProfileByUid(uid)!!
        assertEquals(0.0, profileBefore.focusPoints, 0.01)

        val pointsToAdd = 23.9
        repository.updateFocusPoints(uid = uid, points = pointsToAdd, addToLeaderboard = true)

        val profileAfter = repository.getProfileByUid(uid)!!
        assertEquals(pointsToAdd, profileAfter.focusPoints, 0.01)
        assertEquals(pointsToAdd, profileAfter.weeklyPoints, 0.01)
      }

  /** Check that adding focus points without adding to weekly points only adds to global points */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun addFocusPointsNoLeaderboardWorks() =
      runTest(timeout = 120.seconds) {
        val uid = FirebaseEmulator.auth.currentUser!!.uid
        repository.initProfileIfMissing(uid, "pic.png")

        val profileBefore = repository.getProfileByUid(uid)!!
        assertEquals(0.0, profileBefore.focusPoints, 0.01)

        val pointsToAdd = 23.9
        repository.updateFocusPoints(uid = uid, points = pointsToAdd, addToLeaderboard = false)

        val profileAfter = repository.getProfileByUid(uid)!!
        assertEquals(pointsToAdd, profileAfter.focusPoints, 0.01)
        assertEquals(0.0, profileAfter.weeklyPoints, 0.01)
      }

  @Test
  fun addPendingSentFriendUid_addsUidToList() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // Create User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    val targetUid = "targetUser123"

    // Add pending request
    repo.addPendingSentFriendUid(userAUid, targetUid)

    val profileA = repo.getProfileByUid(userAUid)

    assertNotNull(profileA)
    assertTrue(profileA!!.pendingSentFriendsUids.contains(targetUid))
  }

  @Test
  fun removePendingSentFriendUid_removesUidFromList() = runTest {
    val auth = FirebaseEmulator.auth
    val firestore = FirebaseEmulator.firestore
    val storage = FirebaseEmulator.storage
    val repo = ProfileRepositoryFirestore(firestore, storage)

    // Create User A
    auth.signInAnonymously().await()
    val userAUid = auth.currentUser!!.uid
    repo.initProfileIfMissing(userAUid, "alice.png")

    val targetUid = "targetUser456"

    // First add it
    repo.addPendingSentFriendUid(userAUid, targetUid)

    // Now remove it
    repo.removePendingSentFriendUid(userAUid, targetUid)

    val profileA = repo.getProfileByUid(userAUid)

    assertNotNull(profileA)
    assertFalse(profileA!!.pendingSentFriendsUids.contains(targetUid))
  }
}
