package com.ho.holive.domain.usecase

import com.ho.holive.core.common.AppResult
import com.ho.holive.domain.model.LivePlatform
import com.ho.holive.domain.repository.LiveRepository
import javax.inject.Inject

class RefreshRoomsUseCase @Inject constructor(
    private val repository: LiveRepository,
) {
    suspend operator fun invoke(platform: LivePlatform): AppResult<Unit> = repository.refreshRooms(platform)
}
