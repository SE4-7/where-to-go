package com.example.wheretogo

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class ModifyTasteFragment : Fragment() {
    lateinit var foodRecyclerView: RecyclerView
    lateinit var foodViewAdapter: FoodViewAdapter
    lateinit var foodViewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_taste, container, false)

        foodViewManager = GridLayoutManager(context, 2)
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
        foodRecyclerView = view.findViewById(R.id.recyclerview_food)
        foodRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = foodViewManager
            adapter = foodViewAdapter
        }

        val updateButton: Button = view.findViewById(R.id.button_selection_complete)
        updateButton.text = "수정하기"
        updateButton.setOnClickListener {
            UpdateContents().execute(
                "http://$IP_ADDRESS/wheretogo/update.php",
                "update user set choice='${foodViewAdapter.getChoice()}' where id='$userId'"
            )

            val fm = activity?.supportFragmentManager
            fm?.beginTransaction()?.remove(this)?.commit()
            fm?.popBackStack()
        }

        return view
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
                val resId = resources.getIdentifier(item.imageSrc, "drawable", activity?.packageName)
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
        }
    }
}