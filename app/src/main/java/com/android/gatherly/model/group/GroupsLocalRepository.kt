package com.android.gatherly.model.group

import kotlin.String

/**
 * Simplified in-memory local implementation of [GroupsRepository].
 *
 * Used for local testing or offline mode. Only implements the methods actually needed in the
 * current app logic.
 */
class GroupsLocalRepository : GroupsRepository {

  private val groups: MutableList<Group> = mutableListOf()

  private var counter = 0

  override fun getNewId(): String {
    return (counter++).toString()
  }

  override suspend fun getAllGroups(): List<Group> {
    return groups
  }

  override suspend fun getUserGroups(): List<Group> {
    TODO("Not yet implemented")
  }

  override suspend fun getGroup(groupId: String): Group {
    return groups.find { it.gid == groupId }
        ?: throw NoSuchElementException("GroupsLocalRepository: Group not found")
  }

  override suspend fun addGroup(group: Group) {
    groups.add(group)
  }

  override suspend fun editGroup(groupId: String, newValue: Group) {
    val index = groups.indexOfFirst { it.gid == groupId }
    if (index != -1) {
      groups[index] = newValue
    } else {
      throw NoSuchElementException("GroupsLocalRepository: Group not found")
    }
  }

  override suspend fun deleteGroup(groupId: String) {
    val index = groups.indexOfFirst { it.gid == groupId }
    if (index != -1) {
      groups.removeAt(index)
    } else {
      throw NoSuchElementException("GroupsLocalRepository: Group not found")
    }
  }

  override suspend fun addMember(groupId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun removeMember(groupId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun addAdmin(groupId: String, userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun removeAdmin(groupId: String, userId: String) {
    TODO("Not yet implemented")
  }
}
