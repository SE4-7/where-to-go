package com.example.wheretogo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.daum.android.map.MapActivity

class LoginActivity : AppCompatActivity() {
    lateinit var loginId: EditText
    lateinit var loginPw: EditText

    private val finishIntervalTime: Long = 2000
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginId = findViewById(R.id.edittext_login_id)
        loginPw = findViewById(R.id.edittext_login_pw)

        val loginButton: Button = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)

            // TODO("로그인 처리")

            startActivity(intent)
        }

        val signUpButton: Button = findViewById(R.id.button_sign_up)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (intervalTime in 0..finishIntervalTime) {
            super.onBackPressed()
            finish()
        } else {
            backPressedTime = tempTime
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}