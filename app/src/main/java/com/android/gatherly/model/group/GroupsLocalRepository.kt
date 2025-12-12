package com.android.gatherly.model.group

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
    return groups // In a real implementation, filter by current user, but for local testing return
    // all since all groups in this repository are created by the current user.
  }

  override suspend fun getGroup(groupId: String): Group {
    return groups.find { it.gid == groupId }
        ?: throw NoSuchElementException("GroupsLocalRepository.getGroup: Group not found")
  }

  override suspend fun addGroup(group: Group) {
    groups.add(group)
  }

  override suspend fun editGroup(groupId: String, newValue: Group) {
    val index = groups.indexOfFirst { it.gid == groupId }
    if (index != -1) {
      groups[index] = newValue
    } else {
      throw NoSuchElementException("GroupsLocalRepository.editGroup: Group not found")
    }
  }

  override suspend fun deleteGroup(groupId: String) {
    val index = groups.indexOfFirst { it.gid == groupId }
    if (index != -1) {
      groups.removeAt(index)
    } else {
      throw NoSuchElementException("GroupsLocalRepository.deleteGroup: Group not found")
    }
  }

  override suspend fun addMember(groupId: String, userId: String) {
    TODO("GroupsLocalRepository.addMember Not yet implemented")
  }

  override suspend fun removeMember(groupId: String, userId: String) {
    val group = getGroup(groupId)
    val removedMember = group.copy(memberIds = group.memberIds.filter { it != userId })

    if (group.adminIds.contains(userId)) {
      removeAdmin(groupId, userId)
    }

    editGroup(groupId, removedMember)
  }

  override suspend fun addAdmin(groupId: String, userId: String) {
    TODO("GroupsLocalRepository.addAdmin Not yet implemented")
  }

  override suspend fun removeAdmin(groupId: String, userId: String) {
    val group = getGroup(groupId)

    if (!group.adminIds.contains(userId) || group.adminIds.size == 1) return

    val removedAdmin = group.copy(adminIds = group.adminIds.filter { it != userId })

    editGroup(groupId, removedAdmin)
  }

  override suspend fun getGroupByName(groupName: String): Group {
    return groups.find { it.name == groupName }
        ?: throw NoSuchElementException("GroupsLocalRepository.getGroupByName: Group not found")
  }
}
