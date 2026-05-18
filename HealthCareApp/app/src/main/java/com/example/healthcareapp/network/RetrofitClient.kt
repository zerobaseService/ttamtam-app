package com.example.healthcareapp.network

import com.example.healthcareapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val BASE_URL = BuildConfig.BASE_URL

    var tokenProvider: (() -> String?) = { null }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = tokenProvider()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            })
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val folderService: DiaryFolderApiService by lazy {
        retrofit.create(DiaryFolderApiService::class.java)
    }

    val journalService: JournalApiService by lazy {
        retrofit.create(JournalApiService::class.java)
    }

    val imageUploadService: ImageUploadApiService by lazy {
        retrofit.create(ImageUploadApiService::class.java)
    }

    val exerciseService: ExerciseApiService by lazy {
        retrofit.create(ExerciseApiService::class.java)
    }
}
