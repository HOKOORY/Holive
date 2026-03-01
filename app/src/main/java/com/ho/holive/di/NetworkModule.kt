package com.ho.holive.di

import com.ho.holive.BuildConfig
import com.ho.holive.data.remote.LiveApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val PRIMARY_HOST = "api.hclyz.com"
    private const val FALLBACK_HOST = "api.zbjk.xyz"


    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_HTTP_LOG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // The upstream HTTP endpoint is unstable with keep-alive connections.
                    .header("Connection", "close")
                    .build()
                try {
                    chain.proceed(request)
                } catch (ioe: IOException) {
                    if (request.method != "GET") throw ioe

                    if (ioe is UnknownHostException && request.url.host == PRIMARY_HOST) {
                        val backupRequest = request.newBuilder()
                            .url(request.url.newBuilder().host(FALLBACK_HOST).build())
                            .build()
                        return@addInterceptor chain.proceed(backupRequest)
                    }

                    Thread.sleep(300)
                    chain.proceed(request)
                }
            }
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideLiveApiService(retrofit: Retrofit): LiveApiService = retrofit.create(LiveApiService::class.java)
}
