package com.example.wheretogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

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

        val submitButton: Button = view.findViewById(R.id.button_submit)
        submitButton.setOnClickListener {
            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.replace(R.id.container_fragment, modifyInfoFragment)?.commit()
        }

        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.remove(this)?.commit()
            fm?.popBackStack()
        }

        return view
    }
}