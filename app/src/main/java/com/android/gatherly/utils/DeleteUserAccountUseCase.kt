package com.android.gatherly.utils

import android.util.Log
import com.android.gatherly.model.event.EventsRepository
import com.android.gatherly.model.focusSession.FocusSessionsRepository
import com.android.gatherly.model.group.GroupsRepository
import com.android.gatherly.model.notification.Notification
import com.android.gatherly.model.notification.NotificationType
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.profile.ProfileRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.google.firebase.Timestamp

/**
 * Use case responsible for fully deleting a user account and all data owned by that user.
 *
 * @property profileRepository Repository responsible for profile data.
 * @property groupsRepository Repository responsible for group data.
 * @property eventsRepository Repository responsible for event data.
 * @property focusSessionsRepository Repository responsible for focus session data.
 * @property todosRepository Repository responsible for todo data.
 * @property notificationsRepository Repository responsible for notification data.
 */
class DeleteUserAccountUseCase(
    private val profileRepository: ProfileRepository,
    private val groupsRepository: GroupsRepository,
    private val eventsRepository: EventsRepository,
    private val focusSessionsRepository: FocusSessionsRepository,
    private val todosRepository: ToDosRepository,
    private val notificationsRepository: NotificationsRepository
) {

  /**
   * Deletes the user account and all associated data.
   *
   * The deletion is performed in the following order:
   * - Groups owned by the user are deleted.
   * - The user is removed from groups they are a member of.
   * - Events created by the user are deleted.
   * - User is removed from participant lists of events he participated in.
   * - Focus sessions created by the user are deleted.
   * - Todos owned by the user are deleted.
   * - Remove friend notifications are send to the user's friends.
   * - The user profile is deleted last.
   *
   * @param userId The UID of the user whose account should be deleted.
   * @throws Exception if any deletion step fails. Partial deletion may have occurred.
   */
  suspend fun deleteUserAccount(userId: String) {
    val userGroups = groupsRepository.getUserGroups()
    val ownedGroups = userGroups.filter { it.creatorId == userId }
    val memberGroups = userGroups.filter { it.creatorId != userId }
    val allEvents = eventsRepository.getAllEvents()
    val ownedEvents = allEvents.filter { it.creatorId == userId }
    val participatingEvents =
        allEvents.filter { it.creatorId != userId && it.participants.contains(userId) }
    val userFocusSessions = focusSessionsRepository.getUserFocusSessions().toList()
    val userTodos = todosRepository.getAllTodos().toList()
    val profile = profileRepository.getProfileByUid(userId)
    val friendsIds = profile?.friendUids?.toList() ?: emptyList()

    try {
      ownedGroups.forEach { group -> groupsRepository.deleteGroup(group.gid) }

      memberGroups.forEach { group -> groupsRepository.removeMember(group.gid, userId) }

      ownedEvents.forEach { event -> eventsRepository.deleteEvent(event.id) }

      participatingEvents.forEach { event -> eventsRepository.removeParticipant(event.id, userId) }

      userFocusSessions.forEach { session ->
        focusSessionsRepository.deleteFocusSession(session.focusSessionId)
      }

      userTodos.forEach { todo -> todosRepository.deleteTodo(todo.uid) }

      friendsIds.forEach { friendsId ->
        notificationsRepository.addNotification(
            Notification(
                id = notificationsRepository.getNewId(),
                type = NotificationType.REMOVE_FRIEND,
                emissionTime = Timestamp.now(),
                senderId = userId,
                relatedEntityId = null,
                recipientId = friendsId,
                wasRead = false))
      }

      profileRepository.deleteProfile(userId)
    } catch (e: Exception) {
      Log.e("DeleteUserAccount", "Failed to fully delete account", e)
      throw e
    }
  }
}
