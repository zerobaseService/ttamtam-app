package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.GoogleLoginRequest
import com.example.healthcareapp.data.UserDto
import com.example.healthcareapp.network.RetrofitClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)

        val idToken = intent.getStringExtra("ID_TOKEN") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val nickname = intent.getStringExtra("USER_NAME") ?: ""
        Log.d("LoadingActivity", "전달받은 토큰: $idToken")

        sendTokenToServer(idToken, email, nickname)
    }

    private fun sendTokenToServer(idToken: String, email: String, nickname: String) {
        val request = GoogleLoginRequest(idToken, email, nickname)

        RetrofitClient.authService.RegisterUser(request).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                MainScope().launch {
                    if (response.isSuccessful) {
                        delay(3000)
                        val intent = Intent(this@LoadingActivity, LoginSuccess::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("LoadingActivity", "응답 코드: ${response.code()}")
                        Log.e("LoadingActivity", "에러 바디: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LoadingActivity, "서버 거절", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                Log.e("LoadingActivity", "네트워크 에러: ${t.message}")
                Toast.makeText(this@LoadingActivity, "네트워크 연결 실패", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
}
