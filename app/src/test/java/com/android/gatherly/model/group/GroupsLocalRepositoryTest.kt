package com.android.gatherly.model.group

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GroupsLocalRepositoryTest {

  private lateinit var repository: GroupsLocalRepository

  @OptIn(ExperimentalCoroutinesApi::class) val testDispatcher = UnconfinedTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    repository = GroupsLocalRepository()
    Dispatchers.setMain(testDispatcher)
  }

  @Test
  fun getNewId_returnsSequentialIds() {
    val id1 = repository.getNewId()
    val id2 = repository.getNewId()
    assertEquals("0", id1)
    assertEquals("1", id2)
  }

  @Test
  fun addGroup_addsGroupToRepository() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        val groups = repository.getAllGroups()
        assertEquals(1, groups.size)
        assertEquals(GroupsLocalRepositoryTestData.group1, groups[0])
      }

  @Test
  fun getAllGroups_returnsAllGroups() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        repository.addGroup(GroupsLocalRepositoryTestData.group2)
        val groups = repository.getAllGroups()
        assertEquals(2, groups.size)
      }

  @Test
  fun getUserGroups_returnsAllGroups() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        repository.addGroup(GroupsLocalRepositoryTestData.group2)
        val groups = repository.getUserGroups()
        assertEquals(2, groups.size)
      }

  @Test
  fun getGroup_returnsCorrectGroup() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        val group = repository.getGroup(GroupsLocalRepositoryTestData.group1.gid)
        assertEquals(GroupsLocalRepositoryTestData.group1, group)
      }

  @Test(expected = NoSuchElementException::class)
  fun getGroup_throwsWhenGroupNotFound() =
      runTest(timeout = 120.seconds) { repository.getGroup("nonexistent") }

  @Test
  fun editGroup_updatesGroup() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        val updated = GroupsLocalRepositoryTestData.group1.copy(name = "Updated Name")
        repository.editGroup(GroupsLocalRepositoryTestData.group1.gid, updated)
        val result = repository.getGroup(GroupsLocalRepositoryTestData.group1.gid)
        assertEquals("Updated Name", result.name)
      }

  @Test(expected = NoSuchElementException::class)
  fun editGroup_throwsWhenGroupNotFound() =
      runTest(timeout = 120.seconds) {
        repository.editGroup("nonexistent", GroupsLocalRepositoryTestData.group1)
      }

  @Test
  fun deleteGroup_removesGroup() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        repository.deleteGroup(GroupsLocalRepositoryTestData.group1.gid)
        assertEquals(0, repository.getAllGroups().size)
      }

  @Test(expected = NoSuchElementException::class)
  fun deleteGroup_throwsWhenGroupNotFound() =
      runTest(timeout = 120.seconds) { repository.deleteGroup("nonexistent") }

  @Test
  fun getGroupByName_returnsCorrectGroup() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        val group = repository.getGroupByName(GroupsLocalRepositoryTestData.group1.name)
        assertEquals(GroupsLocalRepositoryTestData.group1, group)
      }

  @Test(expected = NoSuchElementException::class)
  fun getGroupByName_throwsWhenGroupNotFound() =
      runTest(timeout = 120.seconds) { repository.getGroupByName("Nonexistent Group") }

  @Test
  fun addMember_addsUserToMemberList() =
      runTest(timeout = 120.seconds) {
        repository.addGroup(GroupsLocalRepositoryTestData.group1)
        repository.addMember(GroupsLocalRepositoryTestData.group1.gid, "user2")
        val group = repository.getGroup(GroupsLocalRepositoryTestData.group1.gid)
        assertTrue(group.memberIds.contains("user2"))
      }

  @Test
  fun removeMember_removesFromBothLists() =
      runTest(timeout = 120.seconds) {
        val groupWithMember =
            GroupsLocalRepositoryTestData.group1.copy(
                memberIds = listOf("user1", "user2"), adminIds = listOf("user1", "user2"))
        repository.addGroup(groupWithMember)
        repository.removeMember(groupWithMember.gid, "user2")
        val group = repository.getGroup(groupWithMember.gid)
        assertFalse(group.memberIds.contains("user2"))
        assertFalse(group.adminIds.contains("user2"))
      }

  @Test
  fun addAdmin_addsUserToAdminList() =
      runTest(timeout = 120.seconds) {
        val groupWithMember =
            GroupsLocalRepositoryTestData.group1.copy(memberIds = listOf("user1", "user2"))
        repository.addGroup(groupWithMember)
        repository.addAdmin(groupWithMember.gid, "user2")
        val group = repository.getGroup(groupWithMember.gid)
        assertTrue(group.adminIds.contains("user2"))
      }

  @Test
  fun removeAdmin_removesFromAdminListOnly() =
      runTest(timeout = 120.seconds) {
        val groupWithAdmin =
            GroupsLocalRepositoryTestData.group1.copy(
                memberIds = listOf("user1", "user2"), adminIds = listOf("user1", "user2"))
        repository.addGroup(groupWithAdmin)
        repository.removeAdmin(groupWithAdmin.gid, "user2")
        val group = repository.getGroup(groupWithAdmin.gid)
        assertTrue(group.memberIds.contains("user2"))
        assertFalse(group.adminIds.contains("user2"))
      }
}
