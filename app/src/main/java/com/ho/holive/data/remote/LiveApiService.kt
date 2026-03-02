package com.ho.holive.data.remote

import com.ho.holive.data.remote.dto.LiveRoomListResponse
import com.ho.holive.data.remote.dto.PlatformListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface LiveApiService {
    @GET("mf/json.txt")
    suspend fun getPlatforms(): PlatformListResponse

    @GET("mf/{address}")
    suspend fun getPlatformRooms(
        @Path("address") address: String,
    ): LiveRoomListResponse
}
