package com.ho.holive.data.remote

import com.ho.holive.data.remote.dto.GithubReleaseResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubApiService {
    @GET
    suspend fun getLatestRelease(@Url url: String): GithubReleaseResponse
}
