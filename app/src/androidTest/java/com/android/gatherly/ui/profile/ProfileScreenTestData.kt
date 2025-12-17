package com.android.gatherly.ui.profile

import com.android.gatherly.model.group.Group
import com.android.gatherly.model.profile.Profile

/**
 * Shared test data used by Profile related UI tests.
 *
 * Provides reusable fake profiles and groups to avoid duplication across test cases and improve
 * readability.
 */
object ProfileScreenTestData {

  val profile1 =
      Profile(
          name = "Default User",
          username = "defaultusername",
          school = "University",
          schoolYear = "Year",
          friendUids = emptyList(),
          groupIds = listOf("g1", "g2"),
          bio = "profileScreenTestBio",
      )

  val profile2 =
      Profile(
          uid = "userProfile_testUid",
          name = "Alice",
          username = "userProfile_alice",
          school = "EPFL",
          schoolYear = "2025",
          friendUids = emptyList(),
          bio = "userProfile_bio")

  val group1 =
      Group(
          gid = "g1",
          creatorId = "u1",
          name = "Group One",
          memberIds = listOf("a", "b", "c"),
          adminIds = listOf())
  val group2 =
      Group(
          gid = "g2",
          creatorId = "u1",
          name = "Group Two",
          memberIds = listOf("a"),
          adminIds = listOf())
}
