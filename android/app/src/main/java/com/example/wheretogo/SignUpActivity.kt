package com.example.wheretogo

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Food(var name: String, var isSelected: Boolean = false, var imageSrc: String = "logo01")

class SignUpActivity : AppCompatActivity() {
    lateinit var signUpId: EditText
    lateinit var signUpPw: EditText
    lateinit var signUpName: EditText
    lateinit var signUpBirth: EditText
    lateinit var radioGroupSex: RadioGroup

    lateinit var tasteLayout: LinearLayout
    lateinit var foodRecyclerView: RecyclerView
    lateinit var foodViewAdapter: FoodViewAdapter
    lateinit var foodViewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpId = findViewById(R.id.edittext_sign_up_id)
        signUpPw = findViewById(R.id.edittext_sign_up_pw)
        signUpName = findViewById(R.id.edittext_sign_up_name)
        signUpBirth = findViewById(R.id.edittext_sign_up_birth)
        signUpBirth.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
                signUpBirth.setText("$year-${month+1}-$dayOfMonth")
            }
            datePickerDialog.show()
        }

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
            val name = signUpName.editableText.toString()
            val birth = signUpBirth.editableText.toString()
            //sex

            tasteLayout.visibility = View.VISIBLE
        }

        tasteLayout = findViewById(R.id.layout_taste)
        tasteLayout.visibility = View.GONE
        foodViewManager = GridLayoutManager(this, 2)
        foodViewAdapter = FoodViewAdapter()
        foodViewAdapter.addItem(Food("비빔밥", false, "bibimbap"))
        foodViewAdapter.addItem(Food("치킨", false, "chicken"))
        foodViewAdapter.addItem(Food("햄버거", false, "hamburger"))
        foodViewAdapter.addItem(Food("짜장면", false, "jajangmyeon"))
        foodViewAdapter.addItem(Food("팟타이", false, "pad_thai"))
        foodViewAdapter.addItem(Food("피자", false, "pizza"))
        foodViewAdapter.addItem(Food("회", false, "sashimi"))
        foodViewAdapter.addItem(Food("설렁탕", false, "soup"))
        foodViewAdapter.addItem(Food("스테이크", false, "steak"))
        foodViewAdapter.addItem(Food("떡볶이", false, "tteokbokki"))
        foodRecyclerView = findViewById(R.id.recyclerview_food)
        foodRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = foodViewManager
            adapter = foodViewAdapter
        }

        val selectionCompleteButton: Button = findViewById(R.id.button_selection_complete)
        selectionCompleteButton.setOnClickListener {
            //TODO("회원가입 처리")

            finish()
        }
    }

    inner class FoodViewAdapter() : RecyclerView.Adapter<FoodViewAdapter.ViewHolder>() {
        private val items = ArrayList<Food>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val foodName: TextView = itemView.findViewById(R.id.textview_food_name)
            private val foodImage: ImageView = itemView.findViewById(R.id.imageview_food_image)
            private val foodLayout: LinearLayout = itemView.findViewById(R.id.layout_food)

            init {
                itemView.setOnClickListener {
                    if (!items[adapterPosition].isSelected) {
                        foodLayout.setBackgroundColor(Color.rgb(255,230,153))
                        items[adapterPosition].isSelected = true
                    } else {
                        foodLayout.setBackgroundColor(Color.WHITE)
                        items[adapterPosition].isSelected = false
                    }
                }
            }

            fun setItem(item: Food) {
                foodName.text = item.name
                val resId = resources.getIdentifier(item.imageSrc, "drawable", packageName)
                foodImage.setImageResource(resId)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.food_info, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size

        fun addItem(item: Food) { items.add(item) }
    }
}