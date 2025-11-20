package com.android.gatherly.utils

import com.android.gatherly.model.profile.ProfileRepository

suspend fun addFriend(profileRepository: ProfileRepository, friend: String, userId: String) {
  profileRepository.addFriend(friend, userId)
  profileRepository.incrementAddedFriend(userId)
}
