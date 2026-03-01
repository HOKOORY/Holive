package com.ho.holive.domain.usecase

import com.ho.holive.core.common.AppResult
import com.ho.holive.domain.model.LiveRoomDetail
import com.ho.holive.domain.repository.LiveRepository
import javax.inject.Inject

class GetRoomDetailUseCase @Inject constructor(
    private val repository: LiveRepository,
) {
    suspend operator fun invoke(roomId: String): AppResult<LiveRoomDetail> = repository.getRoomDetail(roomId)
}
