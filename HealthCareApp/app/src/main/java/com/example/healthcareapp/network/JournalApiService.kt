package com.example.healthcareapp.network

import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CompleteJournalRequest
import com.example.healthcareapp.data.CreateJournalRequest
import com.example.healthcareapp.data.JournalCreateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface JournalApiService {

    @POST("/api/journals")
    fun createJournal(@Body request: CreateJournalRequest): Call<ApiResponse<JournalCreateResponse>>

    @POST("/api/journals/complete")
    fun completeJournal(@Body request: CompleteJournalRequest): Call<ApiResponse<Any>>
}
