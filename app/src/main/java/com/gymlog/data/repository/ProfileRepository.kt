package com.gymlog.data.repository

import com.gymlog.data.local.UserProfileDao
import com.gymlog.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val userProfileDao: UserProfileDao,
) {
    fun observeProfile(): Flow<UserProfileEntity?> = userProfileDao.observeProfile()

    suspend fun saveProfile(profile: UserProfileEntity) {
        userProfileDao.upsertProfile(profile.copy(id = 1L))
    }
}
