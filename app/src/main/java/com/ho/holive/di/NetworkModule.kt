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
import java.util.zip.Inflater
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.GzipSource
import okio.InflaterSource
import okio.buffer
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val PRIMARY_HOST = "vercel.hokoory.top"
    private const val FALLBACK_HOST = "vercel.hokoory.top"


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
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // The upstream HTTP endpoint is unstable with keep-alive connections.
                    .header("Connection", "close")
                    .build()
                try {
                    proceedWithDecompression(chain, request)
                } catch (ioe: IOException) {
                    if (request.method != "GET") throw ioe

                    if (ioe is UnknownHostException && request.url.host == PRIMARY_HOST) {
                        val backupRequest = request.newBuilder()
                            .url(request.url.newBuilder().host(FALLBACK_HOST).build())
                            .build()
                        return@addInterceptor proceedWithDecompression(chain, backupRequest)
                    }

                    Thread.sleep(300)
                    proceedWithDecompression(chain, request)
                }
            }
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun proceedWithDecompression(
        chain: okhttp3.Interceptor.Chain,
        request: okhttp3.Request,
    ): Response {
        val response = chain.proceed(request)
        val body = response.body ?: return response
        val encoding = response.header("Content-Encoding")?.trim()?.lowercase() ?: return response

        val decompressedBytes = when (encoding) {
            "gzip" -> GzipSource(body.source()).buffer().use { it.readByteArray() }
            "deflate" -> InflaterSource(body.source(), Inflater()).buffer().use { it.readByteArray() }
            else -> return response
        }

        return response.newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedBytes.toResponseBody(body.contentType()))
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
