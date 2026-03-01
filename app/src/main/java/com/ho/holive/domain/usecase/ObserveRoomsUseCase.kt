package com.ho.holive.domain.usecase

import androidx.paging.PagingData
import com.ho.holive.domain.model.LiveRoom
import com.ho.holive.domain.repository.LiveRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRoomsUseCase @Inject constructor(
    private val repository: LiveRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<LiveRoom>> = repository.observePagedRooms(query)
}
