package com.example.wheretogo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    lateinit var signUpId: EditText
    lateinit var signUpPw: EditText
    lateinit var signUpAge: EditText
    lateinit var radioGroupSex: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpId = findViewById(R.id.edittext_sign_up_id)
        signUpPw = findViewById(R.id.edittext_sign_up_pw)
        signUpAge = findViewById(R.id.edittext_sign_up_age)

        var sex = ""

        radioGroupSex = findViewById(R.id.radiogroup_sex)
        radioGroupSex.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radiobutton_man -> {
                    sex = "남"
                }
                R.id.radiobutton_women -> {
                    sex = "여"
                }
            }
        }

        val signUpButton: Button = findViewById(R.id.button_sign_up)
        signUpButton.setOnClickListener {
            val id = signUpId.editableText.toString()
            val pw = signUpPw.editableText.toString()
            val age = signUpAge.editableText.toString()
            sex

            TODO("회원가입 처리")

            finish()
        }
    }
}