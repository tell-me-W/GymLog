package com.gymlog.data.repository

import com.gymlog.data.local.UserProfileDao
import com.gymlog.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRepositoryTest {
    @Test
    fun savesAndObservesProfile() = runTest {
        val dao = FakeUserProfileDao()
        val repository = ProfileRepository(dao)
        val profile = UserProfileEntity(heightCm = 180.0, weightKg = 75.5, gender = "남성", age = 33)

        repository.saveProfile(profile)

        assertEquals(profile, dao.profile.value)
    }
}

private class FakeUserProfileDao : UserProfileDao {
    val profile = MutableStateFlow<UserProfileEntity?>(null)

    override fun observeProfile(): Flow<UserProfileEntity?> = profile

    override suspend fun upsertProfile(profile: UserProfileEntity) {
        this.profile.value = profile
    }
}
