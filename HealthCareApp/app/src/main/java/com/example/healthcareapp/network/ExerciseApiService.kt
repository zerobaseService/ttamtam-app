package com.example.healthcareapp.network

import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.ExerciseSummaryDto
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExerciseApiService {

    @GET("/api/exercises/all")
    fun getAllExercises(): Call<ApiResponse<List<ExerciseSummaryDto>>>

    @POST("/api/exercises/{id}/favorite")
    fun addFavorite(@Path("id") id: String): Call<Void>

    @DELETE("/api/exercises/{id}/favorite")
    fun removeFavorite(@Path("id") id: String): Call<Void>
}
