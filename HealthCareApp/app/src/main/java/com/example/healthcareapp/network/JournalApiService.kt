package com.example.healthcareapp.network

import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CompleteJournalRequest
import com.example.healthcareapp.data.CreateJournalRequest
import com.example.healthcareapp.data.JournalCreateResponse
import com.example.healthcareapp.data.JournalDetailResponse
import com.example.healthcareapp.data.JournalSummaryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JournalApiService {

    @POST("/api/journals")
    fun createJournal(@Body request: CreateJournalRequest): Call<ApiResponse<JournalCreateResponse>>

    @POST("/api/journals/complete")
    fun completeJournal(@Body request: CompleteJournalRequest): Call<ApiResponse<Any>>

    @GET("/api/journals")
    fun getJournals(
        @Query("date") date: String? = null,
        @Query("folderId") folderId: Long? = null,
        @Query("unfiled") unfiled: Boolean? = null
    ): Call<ApiResponse<List<JournalSummaryResponse>>>

    @GET("/api/journals/{id}")
    fun getJournalDetail(@Path("id") journalId: Long): Call<ApiResponse<JournalDetailResponse>>
}
