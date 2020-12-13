package com.example.wheretogo

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class ModifyInfoFragment : Fragment() {
    lateinit var myId: TextView
    lateinit var myPw: EditText
    lateinit var myName: TextView
    lateinit var myBirth: TextView
    lateinit var myGender: RadioGroup
    lateinit var menRadioButton: RadioButton
    lateinit var womenRadioButton: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_info, container, false)

        SelectUserInfo().execute(
            "http://$IP_ADDRESS/wheretogo/select_user.php",
            "select * from user where id='$userId'"
        )

        myId = view.findViewById(R.id.idLocation)
        myPw = view.findViewById(R.id.passwordLocation)
        myName = view.findViewById(R.id.nameLocation)
        myBirth = view.findViewById(R.id.birthLocation)
        myGender = view.findViewById(R.id.radiogroup_sex_edit)
        menRadioButton = view.findViewById(R.id.radiobutton_man_edit)
        womenRadioButton = view.findViewById(R.id.radiobutton_women_edit)

        val pwEditButton: Button = view.findViewById(R.id.button_edit_pw)
        pwEditButton.setOnClickListener {
            UpdateContents().execute(
                "http://$IP_ADDRESS/wheretogo/update.php",
                "update user set pw='${myPw.text}' where id='$userId'"
            )
        }
        val sexEditButton: Button = view.findViewById(R.id.button_edit_sex)
        sexEditButton.setOnClickListener {
            when (myGender.checkedRadioButtonId) {
                R.id.radiobutton_man_edit -> {
                    UpdateContents().execute(
                        "http://$IP_ADDRESS/wheretogo/update.php",
                        "update user set gender='남자' where id='$userId'"
                    )
                }
                R.id.radiobutton_women_edit -> {
                    UpdateContents().execute(
                        "http://$IP_ADDRESS/wheretogo/update.php",
                        "update user set gender='여자' where id='$userId'"
                    )
                }
            }
        }
        val submitButton: Button = view.findViewById(R.id.button_submit)
        submitButton.setOnClickListener {
            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.remove(this)?.commit()
            fm?.popBackStack()
        }
        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.remove(this)?.commit()
            fm?.popBackStack()
        }

        return view
    }

    inner class SelectUserInfo : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val query = p0[1]

            val serverURL = p0[0]
            val postParameters = "query=$query"

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

            if (result == "empty") {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val id = item.getString("id")
                        val pw = item.getString("pw")
                        val name = item.getString("name")
                        val gender = item.getString("gender")
                        val birth = item.getString("birth")
                        val choice = item.getString("choice")

                        myId.text = id
                        myPw.setText(pw)
                        myName.text = name
                        myBirth.text = birth
                        if (gender == "남자") {
                            menRadioButton.isChecked = true
                        } else {
                            womenRadioButton.isChecked = true
                        }
                    }
                } catch (e: JSONException) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class UpdateContents : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val query = p0[1]

            val serverURL = p0[0]
            val postParameters = "query=$query"

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

            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
        }
    }
}