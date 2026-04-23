package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareapp.data.FolderResponse
import com.example.healthcareapp.data.InviteAcceptRequest
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InviteAcceptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            finish()
            return
        }

        val folderId = uri.getQueryParameter("folderId")?.toLongOrNull()
        val token = uri.getQueryParameter("token")

        if (folderId == null || token.isNullOrBlank()) {
            Toast.makeText(this, "유효하지 않은 초대 링크입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val accessToken = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .getString("access_token", null)

        if (accessToken == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        acceptInvite(folderId, token)
    }

    private fun acceptInvite(folderId: Long, token: String) {
        RetrofitClient.folderService.acceptInvite(InviteAcceptRequest(folderId, token))
            .enqueue(object : Callback<FolderResponse> {
                override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@InviteAcceptActivity, "폴더에 참여했습니다!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@InviteAcceptActivity, FolderActivity::class.java))
                        finish()
                    } else {
                        val msg = when (response.code()) {
                            400 -> {
                                val errorBody = response.errorBody()?.string() ?: ""
                                when {
                                    errorBody.contains("ALREADY_MEMBER") -> "이미 참여 중인 폴더입니다."
                                    errorBody.contains("INVALID_TOKEN") -> "유효하지 않은 초대 링크입니다."
                                    errorBody.contains("FOLDER_CLOSED") -> "닫힌 폴더입니다."
                                    else -> "초대 수락 실패"
                                }
                            }
                            409 -> "이미 2명이 참여 중입니다."
                            else -> "초대 수락 실패 (${response.code()})"
                        }
                        Toast.makeText(this@InviteAcceptActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                    Toast.makeText(this@InviteAcceptActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }
}
