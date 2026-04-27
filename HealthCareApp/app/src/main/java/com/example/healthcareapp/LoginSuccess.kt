package com.example.healthcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class LoginSuccess : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.success)

        val StartButton = findViewById<Button>(R.id.StartButton)

        StartButton.setOnClickListener{
            //val intent = Intent(this,FolderActivity::class.java)
            val intent = Intent(this,FolderActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }




}