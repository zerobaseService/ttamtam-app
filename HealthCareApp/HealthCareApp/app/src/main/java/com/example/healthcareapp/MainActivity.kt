package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.healthcareapp.LoadingActivity

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_login_custom)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("273956393814-8q7knfjqnrrlnvihtgbbucj2rar9uojg.apps.googleusercontent.com") // Firebase 연동 시 자동 생성되는 ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!

                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "구글 로그인 실패: ${e.message}")
                    Toast.makeText(this, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }



        val loginButton = findViewById<Button>(R.id.btn_google_login)
        loginButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "${user?.displayName}님, 환영합니다!", Toast.LENGTH_SHORT).show()

                    // LoadingActivity를 실행하면서 토큰을 전달합니다.
                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("ID_TOKEN", idToken)
                    intent.putExtra("USER_EMAIL", user?.email ?: "") // 이메일 추가
                    intent.putExtra("USER_NAME", user?.displayName ?: "")
                    startActivity(intent)
                   // finish()
                } else {
                    Log.e("FirebaseAuth", "인증 실패: ${task.exception?.message}")
                    Toast.makeText(this, "인증 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
