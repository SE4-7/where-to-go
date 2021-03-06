package com.example.wheretogo

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class Food(var number: Int, var name: String, var isSelected: Boolean = false, var imageSrc: String = "logo01")

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

        var gender = ""

        radioGroupSex = findViewById(R.id.radiogroup_sex)
        radioGroupSex.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radiobutton_man -> { gender = "남자" }
                R.id.radiobutton_women -> { gender = "여자" }
            }
        }

        val signUpButton: Button = findViewById(R.id.button_sign_up)
        signUpButton.setOnClickListener {
            tasteLayout.visibility = View.VISIBLE
        }

        tasteLayout = findViewById(R.id.layout_taste)
        tasteLayout.visibility = View.GONE
        foodViewManager = GridLayoutManager(this, 2)
        foodViewAdapter = FoodViewAdapter()
        foodViewAdapter.addItem(Food(0, "비빔밥", false, "bibimbap"))
        foodViewAdapter.addItem(Food(1, "치킨", false, "chicken"))
        foodViewAdapter.addItem(Food(2, "햄버거", false, "hamburger"))
        foodViewAdapter.addItem(Food(3, "짜장면", false, "jajangmyeon"))
        foodViewAdapter.addItem(Food(4, "팟타이", false, "pad_thai"))
        foodViewAdapter.addItem(Food(5, "피자", false, "pizza"))
        foodViewAdapter.addItem(Food(6, "회", false, "sashimi"))
        foodViewAdapter.addItem(Food(7, "설렁탕", false, "soup"))
        foodViewAdapter.addItem(Food(8, "스테이크", false, "steak"))
        foodViewAdapter.addItem(Food(9, "떡볶이", false, "tteokbokki"))
        foodRecyclerView = findViewById(R.id.recyclerview_food)
        foodRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = foodViewManager
            adapter = foodViewAdapter
        }

        val selectionCompleteButton: Button = findViewById(R.id.button_selection_complete)
        selectionCompleteButton.setOnClickListener {
            val id = signUpId.editableText.toString()
            val pw = signUpPw.editableText.toString()
            val name = signUpName.editableText.toString()
            val birth = signUpBirth.editableText.toString()
            val choice = foodViewAdapter.getChoice()

            SignUp().execute(
                "http://$IP_ADDRESS/wheretogo/signup.php",
                id, pw, name, gender, birth, choice
            )

            finish()
        }
    }

    override fun onBackPressed() {
        if (tasteLayout.visibility == View.GONE) {
            finish()
        } else {
            tasteLayout.visibility = View.GONE
        }
    }

    inner class FoodViewAdapter : RecyclerView.Adapter<FoodViewAdapter.ViewHolder>() {
        private val items = ArrayList<Food>()
        private var count = 0

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val foodName: TextView = itemView.findViewById(R.id.textview_food_name)
            private val foodImage: ImageView = itemView.findViewById(R.id.imageview_food_image)
            private val foodLayout: LinearLayout = itemView.findViewById(R.id.layout_food)

            init {
                itemView.setOnClickListener {
                    if (!items[adapterPosition].isSelected) {
                        if (count < 3) {
                            foodLayout.setBackgroundColor(Color.rgb(255, 230, 153))
                            items[adapterPosition].isSelected = true
                            count++
                        }
                    } else {
                        foodLayout.setBackgroundColor(Color.WHITE)
                        items[adapterPosition].isSelected = false
                        count--
                        if (count < 0) { count = 0 }
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
        fun getChoice(): String {
            var choice = ""
            for (item in items) {
                if (item.isSelected) {
                    choice += "${item.number},"
                }
            }
            if (choice.isNotEmpty() && choice[choice.length-1] == ',' ) {
                choice = choice.substring(0, choice.length-1)
            }
            return choice
        }
    }

    inner class SignUp : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val id = p0[1]
            val pw = p0[2]
            val name = p0[3]
            val gender = p0[4]
            val birth = p0[5]
            val choice = p0[6]

            val serverURL = p0[0]
            val postParameters = "id=$id&pw=$pw&name=$name&gender=$gender&birth=$birth&choice=$choice"

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

            when {
                result?.contains("성공")!! -> {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()

                    finish()
                }
                result.contains("실패") -> {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                }
                result.contains("PRIMARY") -> {
                    signUpId.text.clear()
                }
            }
        }
    }
}