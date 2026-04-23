package com.example.healthcareapp.network

import com.example.healthcareapp.data.FolderResponse
import com.example.healthcareapp.data.InviteAcceptRequest
import com.example.healthcareapp.data.InviteLinkResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DiaryFolderApiService {

    @POST("/api/folders/{folderId}/invite-link")
    fun createInviteLink(@Path("folderId") folderId: Long): Call<InviteLinkResponse>

    @POST("/api/folders/invite/accept")
    fun acceptInvite(@Body request: InviteAcceptRequest): Call<FolderResponse>
}
