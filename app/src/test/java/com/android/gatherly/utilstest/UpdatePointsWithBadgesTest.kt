package com.android.gatherly.utilstest

import com.android.gatherly.model.badge.Badge
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.points.PointsLocalRepository
import com.android.gatherly.model.points.PointsRepository
import com.android.gatherly.model.profile.Profile
import com.android.gatherly.model.profile.ProfileLocalRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.utils.addFriendWithPointsCheck
import com.android.gatherly.utils.incrementBadgeCheckPoints
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdatePointsWithBadgesTest {

  private lateinit var profileRepository: ProfileRepository
  private lateinit var pointsRepository: PointsRepository

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    profileRepository = ProfileLocalRepository()
    pointsRepository = PointsLocalRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun startingBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.TODOS_CREATED.name to 0L)))

    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, aliceUid, BadgeType.TODOS_CREATED)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.STARTING_TODOS_CREATED_BADGE.id), aliceUpdatedProfile.badgeIds)

    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(10.0, points[0].obtained)
  }

  @Test
  fun bronzeBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.TODOS_COMPLETED.name to 2L)))

    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, aliceUid, BadgeType.TODOS_COMPLETED)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.BRONZE_TODOS_COMPLETED_BADGE.id), aliceUpdatedProfile.badgeIds)

    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(30.0, points[0].obtained)
  }

  @Test
  fun silverBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.EVENTS_CREATED.name to 4L)))

    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, aliceUid, BadgeType.EVENTS_CREATED)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.SILVER_EVENTS_CREATED_BADGE.id), aliceUpdatedProfile.badgeIds)
    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(50.0, points[0].obtained)
  }

  @Test
  fun goldBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.EVENTS_PARTICIPATED.name to 9L)))

    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, aliceUid, BadgeType.EVENTS_PARTICIPATED)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.GOLD_EVENTS_PARTICIPATED_BADGE.id), aliceUpdatedProfile.badgeIds)

    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(100.0, points[0].obtained)
  }

  @Test
  fun diamondBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    val bobUsername = "bob_username"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.FRIENDS_ADDED.name to 19L)))

    addFriendWithPointsCheck(profileRepository, pointsRepository, bobUsername, aliceUid)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.DIAMOND_FRIENDS_BADGE.id), aliceUpdatedProfile.badgeIds)
    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(200.0, points[0].obtained)
  }

  @Test
  fun legendBadgeWorks() = runTest {
    val aliceUid = "alice_id"
    profileRepository.addProfile(
        Profile(
            name = "Alice",
            uid = aliceUid,
            username = "alice_username",
            badgeCount = mapOf(BadgeType.FOCUS_SESSIONS_COMPLETED.name to 29L)))

    incrementBadgeCheckPoints(
        profileRepository, pointsRepository, aliceUid, BadgeType.FOCUS_SESSIONS_COMPLETED)
    advanceUntilIdle()

    val aliceUpdatedProfile = profileRepository.getProfileByUid(aliceUid)!!
    assertEquals(listOf(Badge.LEGEND_FOCUS_SESSION_BADGE.id), aliceUpdatedProfile.badgeIds)

    val points = pointsRepository.getAllPoints()
    assertEquals(1, points.size)
    assertEquals(300.0, points[0].obtained)
  }
}
