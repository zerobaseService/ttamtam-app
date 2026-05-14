package com.example.healthcareapp.network

import com.example.healthcareapp.data.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ImageUploadResponse(val imageUrl: String)

interface ImageUploadApiService {

    @Multipart
    @POST("/api/uploads/journal-images")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<ImageUploadResponse>>
}
