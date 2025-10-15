package com.android.gatherly.model.profile


class ProfileLocalRepository : ProfileRepository {

    private val profiles: MutableList<Profile> = mutableListOf()

    override suspend fun getProfileByUid(uid: String): Profile? {
        return profiles.find { it.uid == uid }
    }

    override suspend fun addProfile(profile: Profile) {
        profiles += profile
    }

    override suspend fun updateProfile(profile: Profile) {
        val index = profiles.indexOf(profile)
        if (index == -1) {
            throw IllegalArgumentException()
        } else {
            profiles[index] = profile
        }
    }

    override suspend fun deleteProfile(uid: String) {
        profiles.filter { it.uid != uid }
    }

    override suspend fun isUidRegistered(uid: String): Boolean {
        return profiles.find { it.uid == uid } != null
    }

    override suspend fun findProfilesByUidSubstring(uidSubstring: String): List<Profile> {
        return profiles.filter { it.name.contains(uidSubstring, ignoreCase = true) }
    }
}