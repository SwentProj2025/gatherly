package com.android.gatherly.model.group

object GroupsLocalRepositoryTestData {
  val group1 =
      Group(
          gid = "1",
          creatorId = "user1",
          name = "Test Group 1",
          description = "Description 1",
          memberIds = listOf("user1"),
          adminIds = listOf("user1"))

  val group2 =
      Group(
          gid = "2",
          creatorId = "user2",
          name = "Test Group 2",
          description = "Description 2",
          memberIds = listOf("user2"),
          adminIds = listOf("user2"))
}
