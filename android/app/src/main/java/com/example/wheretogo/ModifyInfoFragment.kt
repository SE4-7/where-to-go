package com.example.wheretogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class ModifyInfoFragment : Fragment() {
    lateinit var id: TextView
    lateinit var pw: EditText
    lateinit var name: TextView
    lateinit var birth: TextView
    lateinit var sex: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modify_info, container, false)

        id = view.findViewById(R.id.idLocation)
        pw = view.findViewById(R.id.passwordLocation)
        name = view.findViewById(R.id.nameLocation)
        birth = view.findViewById(R.id.birthLocation)
        sex = view.findViewById(R.id.radiogroup_sex_edit)

        id.text = ""
        pw.setText("")
        name.text = ""
        birth.text = ""

        val pwEditButton: Button = view.findViewById(R.id.button_edit_pw)
        pwEditButton.setOnClickListener {
            // TODO()
        }
        val sexEditButton: Button = view.findViewById(R.id.button_edit_sex)
        sexEditButton.setOnClickListener {
            when (sex.checkedRadioButtonId) {
                R.id.radiobutton_man_edit -> {
                    // TODO()
                }
                R.id.radiobutton_women_edit -> {
                    // TODO()
                }
            }
        }
        val submitButton: Button = view.findViewById(R.id.button_submit)
        submitButton.setOnClickListener {
            // TODO()

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
}