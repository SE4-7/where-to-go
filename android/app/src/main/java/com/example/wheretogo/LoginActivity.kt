package com.example.wheretogo

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

const val IP_ADDRESS = "211.250.1.30:8888"

var userId = ""

class LoginActivity : AppCompatActivity() {
    lateinit var loginId: EditText
    lateinit var loginPw: EditText

    private val finishIntervalTime: Long = 2000
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initPython()

        loginId = findViewById(R.id.edittext_login_id)
        loginPw = findViewById(R.id.edittext_login_pw)

        val loginButton: Button = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            val id = loginId.editableText.toString()
            val pw = loginPw.editableText.toString()

            userId = id

            Login().execute(
                "http://$IP_ADDRESS/wheretogo/login.php",
                id, pw
            )
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

    private fun initPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

    inner class Login : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val id = p0[1]
            val pw = p0[2]

            val serverURL = p0[0]
            val postParameters = "id=$id&pw=$pw"

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.apply {
                    readTimeout = 5000
                    connectTimeout = 5000
                    requestMethod = "POST"
                    connect()
                }

                val outputStream = httpURLConnection.outputStream
                outputStream.apply {
                    write(postParameters.toByteArray())
                    flush()
                    close()
                }

                val responseStatusCode = httpURLConnection.responseCode

                val inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.inputStream
                } else {
                    httpURLConnection.errorStream
                }

                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String? = null

                line = bufferedReader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()

                return sb.toString()
            } catch (e: Exception) {
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            loginId.text.clear()
            loginPw.text.clear()
            loginPw.clearFocus()

            var intent: Intent? = null

            when (result) {
                "성공" -> {
                    Toast.makeText(applicationContext, "로그인 성공!", Toast.LENGTH_LONG).show()
                    intent = Intent(applicationContext, MapActivity::class.java)
                    intent.putExtra("userId", userId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                "실패" -> {
                    Toast.makeText(applicationContext, "로그인 실패!", Toast.LENGTH_LONG).show()
                }
                else -> {
                    //Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}