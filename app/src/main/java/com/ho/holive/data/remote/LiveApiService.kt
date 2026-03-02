package com.ho.holive.data.remote

import com.ho.holive.data.remote.dto.LiveRoomListResponse
import com.ho.holive.data.remote.dto.PlatformListResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface LiveApiService {
    @GET("json.txt")
    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Encoding: gzip, deflate",
    )
    suspend fun getPlatforms(): PlatformListResponse

    @GET("{address}")
    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Encoding: gzip, deflate",
    )
    suspend fun getPlatformRooms(
        @Path("address") address: String,
    ): LiveRoomListResponse
}
