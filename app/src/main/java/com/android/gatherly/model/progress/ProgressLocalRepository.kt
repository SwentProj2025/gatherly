package com.android.gatherly.model.progress

import com.android.gatherly.model.badge.BadgeRank
import com.android.gatherly.model.badge.BadgeType
import com.android.gatherly.model.profile.ProfileRepository

class ProgressLocalRepository(private val profileRepository: ProfileRepository) :
    ProgressRepository {

  private val progressByUser: MutableMap<String, Progress> = mutableMapOf()

  private fun getOrCreate(uid: String): Progress =
      progressByUser[uid] ?: Progress().also { progressByUser[uid] = it }

  private fun set(uid: String, progress: Progress): Progress {
    progressByUser[uid] = progress
    return progress
  }

  override suspend fun getProgress(uid: String): Progress = getOrCreate(uid)

  override suspend fun incrementCreatedTodo(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(createdTodoCount = it.createdTodoCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.TODOS_CREATED, updated.createdTodoCount)
    return updated.createdTodoCount
  }

  override suspend fun incrementCompletedTodo(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(completedTodoCount = it.completedTodoCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.TODOS_COMPLETED, updated.completedTodoCount)
    return updated.completedTodoCount
  }

  override suspend fun incrementCreatedEvent(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(createdEventCount = it.createdEventCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.EVENTS_CREATED, updated.createdEventCount)
    return updated.createdEventCount
  }

  override suspend fun incrementParticipatedEvent(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(participatedEventCount = it.participatedEventCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.EVENTS_PARTICIPATED, updated.participatedEventCount)
    return updated.participatedEventCount
  }

  override suspend fun incrementCompletedFocusSession(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(completedFocusSessionCount = it.completedFocusSessionCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.FOCUS_SESSIONS_COMPLETED, updated.completedFocusSessionCount)
    return updated.completedFocusSessionCount
  }

  override suspend fun incrementAddedFriend(uid: String): Int {
    val updated =
        getOrCreate(uid)
            .let { it.copy(addedFriendsCount = it.addedFriendsCount + 1) }
            .also { set(uid, it) }

    awardBadge(uid, BadgeType.FRIENDS_ADDED, updated.addedFriendsCount)
    return updated.addedFriendsCount
  }

  // ---------- helpers ----------

  private suspend fun awardBadge(uid: String, type: BadgeType, count: Int) {
    val rank = countToRank(count)
    if (rank == BadgeRank.BLANK) return

    // Fake badge id for tests/local usage: e.g. "TODOS_CREATED_GOLD"
    val badgeId = "${type.name}_${rank.name}"

    val profile = profileRepository.getProfileByUid(uid) ?: return
    profileRepository.addBadge(profile, badgeId)
  }

  private fun countToRank(count: Int): BadgeRank =
      when {
        count >= 30 -> BadgeRank.LEGEND
        count >= 20 -> BadgeRank.DIAMOND
        count >= 10 -> BadgeRank.GOLD
        count >= 5 -> BadgeRank.SILVER
        count >= 3 -> BadgeRank.BRONZE
        count >= 1 -> BadgeRank.STARTING
        else -> BadgeRank.BLANK
      }
}
