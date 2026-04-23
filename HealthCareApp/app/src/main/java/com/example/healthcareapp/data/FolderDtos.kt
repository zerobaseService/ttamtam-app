package com.example.healthcareapp.data

data class InviteLinkResponse(
    val inviteLink: String
)

data class InviteAcceptRequest(
    val folderId: Long,
    val token: String
)

data class FolderMemberResponse(
    val userId: Long,
    val nickname: String
)

data class FolderResponse(
    val folderId: Long,
    val name: String,
    val isShared: Boolean,
    val memberCount: Int,
    val members: List<FolderMemberResponse>,
    val createdAt: String?,
    val updatedAt: String?
)
