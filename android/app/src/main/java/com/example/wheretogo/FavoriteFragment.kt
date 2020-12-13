package com.example.wheretogo

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

interface OnCallbackFromFavorites {
    fun onCallbackFromFavorites(item: PlaceInfo, items: ArrayList<PlaceInfo>)
    fun onCallbackFromFavoritesMarker(item: PlaceInfo, items: ArrayList<PlaceInfo>)
}

class FavoriteFragment : Fragment() {
    lateinit var recyclerview: RecyclerView
    lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewAdapter: ViewAdapter

    lateinit var sortListBy: LinearLayout
    lateinit var sortListByText: TextView

    var onCallbackFromFavoritesListener: OnCallbackFromFavorites? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCallbackFromFavorites) {
            onCallbackFromFavoritesListener = context as OnCallbackFromFavorites
        } else {
            println("리스너 오류!!")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onCallbackFromFavoritesListener = null
    }

    override fun onResume() {
        super.onResume()

        FavoritesTask().execute(
            "http://$IP_ADDRESS/wheretogo/select_favorites.php",
            "select * from favorites where id='$userId'"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter()
        recyclerview = view.findViewById(R.id.recyclerview_favorites)
        recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        sortListBy = view.findViewById(R.id.sort_list_by)
        sortListByText = view.findViewById(R.id.textview_sort_list_by)

        FavoritesTask().execute(
            "http://$IP_ADDRESS/wheretogo/select_favorites.php",
            "select * from favorites where id='$userId'"
        )

        return view
    }

    fun exit() {
        val fm = activity?.supportFragmentManager
        fm?.beginTransaction()?.remove(this)?.commit()
        fm?.popBackStack()
    }

    inner class ViewAdapter : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
        private val items = ArrayList<PlaceInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private var name: TextView = itemView.findViewById(R.id.place_name)
            private var category: TextView = itemView.findViewById(R.id.place_category)
            private var score: TextView = itemView.findViewById(R.id.place_score)
            private var ratingBar: RatingBar = itemView.findViewById(R.id.place_rating_bar)
            private var scoreCount: TextView = itemView.findViewById(R.id.place_score_count)
            private var reviewCount: TextView = itemView.findViewById(R.id.place_review_count)
            private var address: TextView = itemView.findViewById(R.id.place_address)
            private var phone: TextView = itemView.findViewById(R.id.place_phone)
            private var distance: TextView = itemView.findViewById(R.id.place_distance)
            private var favoritesButton: Button = itemView.findViewById(R.id.button_favorites)

            init {
                itemView.setOnClickListener {
                    val item = items[adapterPosition]
                    onCallbackFromFavoritesListener?.onCallbackFromFavorites(item, items)
                    exit()
                }

                favoritesButton.setOnClickListener {
                    val item = items[adapterPosition]
                    if (item.favorites) {
                        item.favorites = false
                        favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
                        UpdateContents().execute(
                            "http://$IP_ADDRESS/wheretogo/update.php",
                            "delete from favorites where id='$userId' and lat=${item.y} and lon=${item.x}"
                        )
                    } else {
                        item.favorites = true
                        favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
                        UpdateContents().execute(
                            "http://$IP_ADDRESS/wheretogo/update.php",
                            "insert into favorites (id, lat, lon) values ('$userId', ${item.y}, ${item.x})"
                        )
                    }
                }
            }

            fun setItem(item: PlaceInfo) {
                name.text = item.placeName
                category.text = categorySubString(item.categoryName)
                score.text = item.scoreAvg.toString()
                ratingBar.rating = item.scoreAvg.toFloat()
                scoreCount.text = item.scoreCount.toString()
                reviewCount.text = item.reviewCount.toString()
                address.text = item.addressName
                phone.text = item.phone
                distance.text = "${item.distance}m"
                if (!item.favorites) {
                    favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
                } else {
                    favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
                }
            }

            private fun categorySubString(category: String): String {
                var subString = ""
                for (i in category.length-1 downTo 0) {
                    val ch = category[i]
                    if (ch != '>') {
                        subString += ch
                    } else {
                        break
                    }
                }
                return subString.reversed()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.list_item, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size

        fun addItem(item: PlaceInfo) { items.add(item) }
        fun findItem(tag: Int): PlaceInfo? {
            for (item in items) {
                if (item.id.toInt() == tag) {
                    return item
                }
            }
            return null
        }

        fun setScoreAvg(lat: Double, lon: Double, scoreAvg: Double) {
            for (item in items) {
                if (lat == item.y && lon == item.x) {
                    item.scoreAvg = scoreAvg
                    break
                }
            }
        }
        fun setScoreCount(lat: Double, lon: Double, scoreCount: Int) {
            for (item in items) {
                if (lat == item.y && lon == item.x) {
                    item.scoreCount = scoreCount
                    break
                }
            }
        }
        fun setReviewCount(lat: Double, lon: Double, reviewCount: Int) {
            for (item in items) {
                if (lat == item.y && lon == item.x) {
                    item.reviewCount = reviewCount
                    break
                }
            }
        }
        fun setFavorites(lat: Double, lon: Double, bool: Boolean) {
            for (item in items) {
                if (lat == item.y && lon == item.x) {
                    item.favorites = bool
                    break
                }
            }
        }

        // 장소들에 대한 평균평점, 평점개수, 리뷰 개수, 즐겨찾기
        fun updateFavorites() {
            for (item in items) {
                UpdateFavorites().execute(
                    "http://$IP_ADDRESS/wheretogo/update_favorites.php",
                    "select lat, lon from favorites where id='$userId' and lat=${item.y} and lon=${item.x}"
                )
            }
        }
        fun updateRating() {
            for (item in items) {
                UpdateRating().execute(
                    "http://$IP_ADDRESS/wheretogo/update_rating.php",
                    "select avg(rating), count(rating), lat, lon from rating where lat=${item.y} and lon=${item.x}"
                )
            }
        }
        fun updateReview() {
            for (item in items) {
                UpdateReview().execute(
                    "http://$IP_ADDRESS/wheretogo/update_review.php",
                    "select count(comment), lat, lon from review where lat=${item.y} and lon=${item.x}"
                )
            }
        }
        fun updateFavoritesPlace() {
            for (item in items) {
                onCallbackFromFavoritesListener?.onCallbackFromFavoritesMarker(item, items)
            }
        }
    }

    inner class FavoritesTask : AsyncTask<String, Void, String>() {
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

            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")

                        RestAPITask().execute(
                            "https://dapi.kakao.com/v2/local/search/category.json?",
                            "x=$lon&y=$lat&radius=1&page=1&size=15&category_group_code=FD6"
                        )
                    }
                } catch (e: JSONException) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class RestAPITask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val page = p0[1]

            try {
                val url = URL(serverURL+page)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.apply {
                    addRequestProperty("Authorization", "KakaoAK 9d869b94af3eec734ef9ef8a8482f8a5")
                    readTimeout = 5000
                    connectTimeout = 5000
                    requestMethod = "GET"
                    connect()
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

            try {
                val jsonObject = JSONObject(result)
                val documentsJsonArray = jsonObject.getJSONArray("documents")

                for (i in 0 until documentsJsonArray.length()) {
                    val documents = documentsJsonArray.getJSONObject(i)
                    val id = documents.getString("id")

                    if (viewAdapter.findItem(id.toInt()) == null) {
                        val item = PlaceInfo(documents)
                        item.addMarker()
                        viewAdapter.addItem(item)
                    }
                }

                viewAdapter.notifyDataSetChanged()

                viewAdapter.updateFavorites()
                viewAdapter.updateRating()
                viewAdapter.updateReview()
            } catch (e: JSONException) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
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

    inner class UpdateRating : AsyncTask<String, Void, String>() {
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

            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val avg = item.getDouble("avg")
                        val count = item.getInt("count")
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")

                        viewAdapter.setScoreAvg(lat, lon, avg)
                        viewAdapter.setScoreCount(lat, lon, count)
                    }

                    viewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class UpdateReview : AsyncTask<String, Void, String>() {
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

            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val count = item.getInt("count")
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")

                        viewAdapter.setReviewCount(lat, lon, count)
                    }

                    viewAdapter.notifyDataSetChanged()
                    viewAdapter.updateFavoritesPlace()
                } catch (e: JSONException) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class UpdateFavorites : AsyncTask<String, Void, String>() {
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

            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")

                        viewAdapter.setFavorites(lat, lon, true)
                    }

                    viewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}