package com.example.wheretogo

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class PasswordCheckFragment : Fragment() {
    lateinit var id: TextView
    lateinit var pw: EditText
    lateinit var pwVisible: CheckBox
    lateinit var modifyInfoFragment: Fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_password_check, container, false)

        id = view.findViewById(R.id.idLocation)
        pw = view.findViewById(R.id.passwordLocation)
        pwVisible = view.findViewById(R.id.checkbox_visible_pw)
        modifyInfoFragment = ModifyInfoFragment()

        id.text = userId
        pwVisible.setOnClickListener {
            if ((it as CheckBox).isChecked) {
                pw.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                pw.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        val submitButton: Button = view.findViewById(R.id.button_submit)
        submitButton.setOnClickListener {
            Login().execute(
                "http://$IP_ADDRESS/wheretogo/login.php",
                userId, pw.text.toString()
            )
            pw.setText("")
            pw.clearFocus()
        }

        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.remove(this)?.commit()
            fm?.popBackStack()
        }

        return view
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

            var intent: Intent? = null

            when (result) {
                "성공" -> {
                    val fm = activity?.supportFragmentManager
                    fm?.beginTransaction()?.replace(R.id.container_fragment, modifyInfoFragment)?.commit()
                }
                "실패" -> {
                    Toast.makeText(context, "비밀번호가 틀렸습니다.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}