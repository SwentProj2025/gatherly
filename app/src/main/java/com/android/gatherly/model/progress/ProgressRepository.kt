package com.android.gatherly.model.progress

interface ProgressRepository {

  suspend fun getProgress(uid: String): Progress

  suspend fun incrementCreatedTodo(uid: String): Int

  suspend fun incrementCompletedTodo(uid: String): Int

  suspend fun incrementCreatedEvent(uid: String): Int

  suspend fun incrementParticipatedEvent(uid: String): Int

  suspend fun incrementCompletedFocusSession(uid: String): Int

  suspend fun incrementAddedFriend(uid: String): Int
}
