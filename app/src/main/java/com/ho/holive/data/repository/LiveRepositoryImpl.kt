package com.ho.holive.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ho.holive.core.common.AppResult
import com.ho.holive.core.common.Logger
import com.ho.holive.data.local.dao.LiveRoomDao
import com.ho.holive.data.local.entity.LiveRoomEntity
import com.ho.holive.data.remote.LiveApiService
import com.ho.holive.domain.model.LivePlatform
import com.ho.holive.domain.model.LiveRoom
import com.ho.holive.domain.model.LiveRoomDetail
import com.ho.holive.domain.repository.LiveRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LiveRepositoryImpl @Inject constructor(
    private val apiService: LiveApiService,
    private val liveRoomDao: LiveRoomDao,
) : LiveRepository {

    override fun observePagedRooms(query: String): Flow<PagingData<LiveRoom>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 3,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { liveRoomDao.pagingSource(query) },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun getPlatforms(): AppResult<List<LivePlatform>> {
        return try {
            val platforms = apiService.getPlatforms().platforms.map { platform ->
                LivePlatform(
                    title = platform.title,
                    address = platform.address,
                    iconUrl = platform.iconUrl.normalizeImageUrl(),
                    onlineCount = platform.onlineCount.toIntOrNull() ?: 0,
                )
            }
            AppResult.Success(platforms)
        } catch (throwable: Throwable) {
            Logger.e("getPlatforms failed", throwable)
            AppResult.Error(throwable)
        }
    }

    override suspend fun refreshRooms(platform: LivePlatform): AppResult<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val roomEntities = apiService.getPlatformRooms(platform.address).anchors.map { anchor ->
                LiveRoomEntity(
                    id = "${platform.address}_${anchor.title}",
                    title = anchor.title,
                    coverUrl = anchor.coverUrl.normalizeImageUrl(),
                    streamUrl = anchor.streamUrl,
                    platformTitle = platform.title,
                    platformIconUrl = platform.iconUrl,
                    viewerCount = platform.onlineCount,
                    updatedAt = now,
                )
            }

            liveRoomDao.clearAll()
            liveRoomDao.insertAll(roomEntities)
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            Logger.e("refreshRooms failed for ${platform.title}", throwable)
            AppResult.Error(throwable)
        }
    }

    override suspend fun getRoomDetail(roomId: String): AppResult<LiveRoomDetail> {
        return try {
            val entity = liveRoomDao.findById(roomId)
                ?: return AppResult.Error(IllegalArgumentException("room not found"))
            AppResult.Success(entity.toDetail())
        } catch (throwable: Throwable) {
            AppResult.Error(throwable)
        }
    }

    override suspend fun getPreviousRoomId(roomId: String): String? {
        val current = liveRoomDao.findById(roomId) ?: return null
        val ids = liveRoomDao.orderedRoomIdsByPlatform(current.platformTitle)
        if (ids.isEmpty()) return null
        val index = ids.indexOf(roomId)
        if (index < 0) return null
        return ids[(index - 1 + ids.size) % ids.size]
    }

    override suspend fun getNextRoomId(roomId: String): String? {
        val current = liveRoomDao.findById(roomId) ?: return null
        val ids = liveRoomDao.orderedRoomIdsByPlatform(current.platformTitle)
        if (ids.isEmpty()) return null
        val index = ids.indexOf(roomId)
        if (index < 0) return null
        return ids[(index + 1) % ids.size]
    }

    private fun String.normalizeImageUrl(): String {
        val value = trim()
        if (value.isEmpty()) return value
        return when {
            value.startsWith("//") -> "https:$value"
            else -> value
        }
    }
}
