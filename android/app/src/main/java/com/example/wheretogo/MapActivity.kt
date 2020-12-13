package com.example.wheretogo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import net.daum.mf.map.api.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

lateinit var mapView: MapView

class PlaceInfo(private val documents: JSONObject) {
    val id = documents.getString("id")
    val placeName = documents.getString("place_name")
    val categoryName = documents.getString("category_name")
    val categoryGroupCode = documents.getString("category_group_code")
    val categoryGroupName = documents.getString("category_group_name")
    val phone = documents.getString("phone")
    val addressName = documents.getString("address_name")
    val roadAddressName = documents.getString("road_address_name")
    val placeUrl = documents.getString("place_url")
    val distance = documents.getString("distance")
    val x = documents.getDouble("x")
    val y = documents.getDouble("y")
    val point = MapPoint.mapPointWithGeoCoord(y, x)
    val item = MapPOIItem()
    var favorites = false
    var scoreAvg = 0.0
    var scoreCount = 0
    var reviewCount = 0

    fun addMarker() {
        item.itemName = placeName
        item.tag = id.toInt()
        item.mapPoint = point
        item.markerType = MapPOIItem.MarkerType.BluePin
        item.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        mapView.addPOIItem(item)
    }

    fun removeMarker() {
        mapView.removePOIItem(item)
    }
}

class MapActivity : AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, MapView.CurrentLocationEventListener, MapView.POIItemEventListener, MapView.MapViewEventListener, OnCallbackFromFavorites {
    // For Map UI
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var logoutButton: LinearLayout
    private lateinit var menuButton: Button
    private lateinit var searchButton: Button
    private lateinit var zoomInButton: Button
    private lateinit var zoomOutButton: Button
    private lateinit var myLocationButton: Button
    private lateinit var listButton: FloatingActionButton
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    // For changing fragment on navigation view
    private lateinit var modifyInfoFragment: Fragment
    private lateinit var passwordCheckFragment: Fragment
    private lateinit var favoriteFragment: Fragment
    private lateinit var ratingManagementFragment: Fragment
    private lateinit var modifyTasteFragment: Fragment
    private lateinit var fragmentContainer: FrameLayout

    // For List UI
    private lateinit var listLayout: FrameLayout
    private lateinit var mapButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var listViewAdapter: ListViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    // For Search UI
    private lateinit var searchLayout: FrameLayout
    private lateinit var backButton: Button
    private lateinit var searchView: SearchView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchViewAdapter: SearchViewAdapter
    private lateinit var searchViewManager: RecyclerView.LayoutManager

    // For Bottom Sheet
    private lateinit var placeName: TextView
    private lateinit var placeCategory: TextView
    private lateinit var placeScore: TextView
    private lateinit var placeRatingBar: RatingBar
    private lateinit var placeScoreCount: TextView
    private lateinit var placeReviewCount: TextView
    private lateinit var placeAddress: TextView
    private lateinit var placePhone: TextView
    private lateinit var placeDistance: TextView
    private lateinit var favoritesButton: Button

    private lateinit var myScoreRatingBar: RatingBar
    private lateinit var averageRatingTextView: TextView
    private lateinit var averageRatingBar: RatingBar
    private lateinit var reviewCount: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewViewManager: RecyclerView.LayoutManager
    private lateinit var reviewViewAdapter: ReviewAdapter
    private lateinit var comment: EditText
    private lateinit var commentButton: Button

    // For sort list
    private lateinit var keywordButton: Button
    private lateinit var baseLocationRadioGroup: RadioGroup
    private lateinit var sortListBy: LinearLayout
    private lateinit var sortListByText: TextView

    var keyword = "양식"
    var page = 0
    var currentX = 0.0
    var currentY = 0.0
    var sort = "accuracy"
    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val intent = intent
        userId = intent.getStringExtra("userId")!!

        // For Map UI
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        logoutButton = findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            Toast.makeText(this, "로그아웃!", Toast.LENGTH_LONG).show()
            finish()
        }
        modifyInfoFragment = ModifyInfoFragment()
        passwordCheckFragment = PasswordCheckFragment()
        favoriteFragment = FavoriteFragment()
        ratingManagementFragment = RatingManagementFragment()
        modifyTasteFragment = ModifyTasteFragment()
        fragmentContainer = findViewById(R.id.container_fragment)

        menuButton = findViewById(R.id.button_menu)
        searchButton = findViewById(R.id.button_search)
        zoomInButton = findViewById(R.id.button_zoom_in)
        zoomOutButton = findViewById(R.id.button_zoom_out)
        myLocationButton = findViewById(R.id.button_my_location)
        listButton = findViewById(R.id.button_list)

        // For List UI
        listLayout = findViewById(R.id.layout_list)
        listLayout.visibility = View.GONE
        viewManager = LinearLayoutManager(applicationContext)
        listViewAdapter = ListViewAdapter()
        recyclerView = findViewById(R.id.recyclerview_list)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = listViewAdapter
        }
        mapButton = findViewById(R.id.button_map)

        // For Search UI
        UpdateSearchHistory().execute(
            "http://$IP_ADDRESS/wheretogo/select_search_history.php",
            "select * from search_history where id='$userId'"
        )
        searchLayout = findViewById(R.id.layout_search)
        searchLayout.visibility = View.GONE
        backButton = findViewById(R.id.button_back)
        keywordButton = findViewById(R.id.button_keyword)
        searchView = findViewById(R.id.searchview_map)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)

                listViewAdapter.clearItems()
                keyword = query!!
                keywordButton.text = keyword
                searchButton.text = keyword

                updatePlace(sort)

                searchView.setQuery("", false)
                searchView.clearFocus()
                searchLayout.visibility = View.GONE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                UpdateContents().execute(
                    "http://$IP_ADDRESS/wheretogo/update.php",
                    "insert into search_history (id, word) values ('$userId', '$query')"
                )

                UpdateSearchHistory().execute(
                    "http://$IP_ADDRESS/wheretogo/select_search_history.php",
                    "select * from search_history where id='$userId'"
                )

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        searchViewAdapter = SearchViewAdapter()
        searchViewManager = LinearLayoutManager(applicationContext)
        searchRecyclerView = findViewById(R.id.recyclerview_search_list)
        searchRecyclerView.apply{
            setHasFixedSize(true)
            layoutManager = searchViewManager
            adapter = searchViewAdapter
        }

        setButtonClickListener()

        startLocationService()

        mapView = MapView(this)
        mapView.setCurrentLocationEventListener(this)
        mapView.setMapViewEventListener(this)
        mapView.setPOIItemEventListener(this)
        mapView.setDefaultCurrentLocationMarker()
        mapView.setShowCurrentLocationMarker(true)
        val mapViewContainer: ViewGroup = findViewById(R.id.mapView)
        mapViewContainer.addView(mapView)

        val bottomSheet: LinearLayout = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        var lp: FrameLayout.LayoutParams = myLocationButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(setViewMargin(10f), 0, 0, setViewMargin(10f))
                        myLocationButton.layoutParams = lp

                        lp = zoomInButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, setViewMargin(50f), setViewMargin(10f), 0)
                        zoomInButton.layoutParams = lp

                        lp = zoomOutButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, setViewMargin(90.5f), setViewMargin(10f), 0)
                        zoomOutButton.layoutParams = lp

                        lp = listButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, 0, setViewMargin(16f), setViewMargin(16f))
                        listButton.layoutParams = lp
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        var lp: FrameLayout.LayoutParams = myLocationButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(setViewMargin(10f), 0, 0, setViewMargin(150f))
                        myLocationButton.layoutParams = lp

                        lp = zoomInButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, setViewMargin(50f), setViewMargin(10f), setViewMargin(100f))
                        zoomInButton.layoutParams = lp

                        lp = zoomOutButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, setViewMargin(90.5f), setViewMargin(10f), setViewMargin(100f))
                        zoomOutButton.layoutParams = lp

                        lp = listButton.layoutParams as FrameLayout.LayoutParams
                        lp.setMargins(0, 0, setViewMargin(16f), setViewMargin(150f))
                        listButton.layoutParams = lp
                    }
                }
            }
        })

        placeName = findViewById(R.id.place_name)
        placeCategory = findViewById(R.id.place_category)
        placeScore = findViewById(R.id.place_score)
        placeRatingBar = findViewById(R.id.place_rating_bar)
        placeScoreCount = findViewById(R.id.place_score_count)
        placeReviewCount = findViewById(R.id.place_review_count)
        placeAddress = findViewById(R.id.place_address)
        placePhone = findViewById(R.id.place_phone)
        placeDistance = findViewById(R.id.place_distance)
        favoritesButton = findViewById(R.id.button_favorites)

        myScoreRatingBar = findViewById(R.id.ratingbar_my_score)
        averageRatingTextView = findViewById(R.id.textview_average_rating)
        averageRatingBar = findViewById(R.id.ratingbar_average_rating)
        reviewCount = findViewById(R.id.textview_review_count)
        reviewViewManager = LinearLayoutManager(this)
        reviewViewAdapter = ReviewAdapter()
        reviewRecyclerView = findViewById(R.id.recyclerview_review)
        reviewRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = reviewViewManager
            adapter = reviewViewAdapter
        }
        comment = findViewById(R.id.edittext_comment)
        commentButton = findViewById(R.id.button_comment)

        baseLocationRadioGroup = findViewById(R.id.radiogroup_base_location)
        sortListBy = findViewById(R.id.sort_list_by)
        sortListByText = findViewById(R.id.textview_sort_list_by)

        registerForContextMenu(sortListBy)
        sortListBy.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.list_sort_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_accuracy -> {
                        sortListByText.text = "정확도순"
                        if (sort != "accuracy") {
                            sort = "accuracy"
                            listViewAdapter.clearItems()
                            updatePlace(sort)
                        }
                    }
                    R.id.menu_distance -> {
                        sortListByText.text = "거리순"
                        if (sort != "distance") {
                            sort = "distance"
                            listViewAdapter.clearItems()
                            updatePlace(sort)
                        }
                    }
                    R.id.menu_rating -> {
                        sortListByText.text = "평점순"
                        listViewAdapter.sortByRating()
                    }
                    else -> false
                }
                Handler().postDelayed({
                    listViewAdapter.notifyDataSetChanged()
                }, 1000)
                true
            }
            popup.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "승인 허가", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "권한 허가 필요", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun setViewMargin(size: Float): Int = (size * resources.displayMetrics.density + 0.5f).toInt()

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            listLayout.visibility == View.VISIBLE -> {
                listLayout.visibility = View.GONE
            }
            searchLayout.visibility == View.VISIBLE -> {
                searchLayout.visibility = View.GONE
            }
            fragmentContainer.visibility == View.VISIBLE -> {
                fragmentContainer.visibility = View.GONE
            }
            bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            else -> {
                finish()
            }
        }
    }

    private fun setButtonClickListener() {
        menuButton.setOnClickListener(this)
        searchButton.setOnClickListener(this)
        zoomInButton.setOnClickListener(this)
        zoomOutButton.setOnClickListener(this)
        myLocationButton.setOnClickListener(this)
        listButton.setOnClickListener(this)

        mapButton.setOnClickListener(this)

        backButton.setOnClickListener(this)

        keywordButton.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.button_menu -> {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            R.id.button_search -> {
                searchLayout.visibility = View.VISIBLE
                searchView.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
            R.id.button_zoom_in -> { mapView.zoomIn(true) }
            R.id.button_zoom_out -> { mapView.zoomOut(true ) }
            R.id.button_my_location -> {
                mapView.setZoomLevel(2, false)
                mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            }
            R.id.button_list -> {
                listViewAdapter.updateFavorites()
                listViewAdapter.updateRating()
                listViewAdapter.updateReview()
                listLayout.visibility = View.VISIBLE
            }
            R.id.button_map -> { listLayout.visibility = View.GONE }
            R.id.button_back -> { searchLayout.visibility = View.GONE }
            R.id.button_keyword -> {
                listLayout.visibility = View.GONE
                searchLayout.visibility = View.VISIBLE
                searchView.isFocusable = true
                searchView.requestFocusFromTouch()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.modify_info -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, passwordCheckFragment).commit()
                fragmentContainer.visibility = View.VISIBLE
            }
            R.id.favorite -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, favoriteFragment).commit()
                fragmentContainer.visibility = View.VISIBLE
            }
            /*
            R.id.rating_management -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, ratingManagementFragment).commit()
                fragmentContainer.visibility = View.VISIBLE
            }
             */
            R.id.modify_taste -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, modifyTasteFragment).commit()
                fragmentContainer.visibility = View.VISIBLE
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // MapView 사용 가능 상태(초기)
    override fun onMapViewInitialized(p0: MapView?) {
        SelectChoice().execute(
            "http://$IP_ADDRESS/wheretogo/select_user.php",
            "select * from user where id='$userId'"
        )

        /*
        // 맵 캐시 사용
        MapView.setMapTilePersistentCacheEnabled(true)

        // 맴 캐시 데이터 삭제
        MapView.clearMapTilePersistentCache()

        // 서버로부터 지도 타일 갱신
        mapView.refreshMapTiles()

        // 지도 타일 이미지 캐쉬 데이터 삭제
        mapView.releaseUnusedMapTileImageResources()
         */
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        //mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    // 현 위치 갱신 시
    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
        currentX = p1?.mapPointGeoCoord?.longitude!!
        currentY = p1.mapPointGeoCoord.latitude
    }

    // 지도 이동이 끝난 경우
    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
        updatePlace(sort)
        Handler().postDelayed({
            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        }, 1000)
    }

    // 마커 클릭 시
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        updateBottomSheet(p1?.tag!!)

        mapView.setMapCenterPointAndZoomLevel(p1.mapPoint, 2, true)
    }

    // 지도 클릭 시
    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) { } // 지도 중심 좌표가 이동한 경우
    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) { }   // 지도 확대/축소 시
    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) { } // 지도 더블 클릭 시
    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) { }  // 지도 길게 클릭 시
    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) { }  // 지도 드래그 시작한 경우
    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) { }    // 지도 드래그 끝낸 경우

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) { }      // 단말의 방향 각도 값
    override fun onCurrentLocationUpdateFailed(p0: MapView?) { }                        // 갱신 실패 시
    override fun onCurrentLocationUpdateCancelled(p0: MapView?) { }                     // 현 위치 기능 취소 시

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) { }
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?, p2: MapPOIItem.CalloutBalloonButtonType?) {}
    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) { }

    fun categorySubString(category: String): String {
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

    private fun updateBottomSheet(tag: Int) {
        val item = listViewAdapter.findItem(tag)

        UpdateComments().execute(
            "http://$IP_ADDRESS/wheretogo/update_comments.php",
            "select review.id, review.lat, review.lon, review.comment, review.date, user.gender, rating.rating from review, user, rating where review.lat=${item?.y} and review.lon=${item?.x} and review.id=user.id and rating.id=review.id and rating.lat=review.lat and rating.lon=rating.lon"
        )

        if (item != null) {
            placeName.text = item.placeName
            placeCategory.text = categorySubString(item.categoryName)
            placeScore.text = item.scoreAvg.toString()
            placeRatingBar.rating = item.scoreAvg.toFloat()
            placeScoreCount.text = item.scoreCount.toString()
            placeReviewCount.text = item.reviewCount.toString()
            placeAddress.text = item.addressName
            placePhone.text = item.phone
            placeDistance.text = "${item.distance}m"
            if (!item.favorites) {
                favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
            } else {
                favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
            }
            favoritesButton.setOnClickListener {
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

            myScoreRatingBar.rating = 0f
            myScoreRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                UpdateContents().execute(
                    "http://$IP_ADDRESS/wheretogo/update.php",
                    "insert into rating (id, lat, lon, rating) values ('$userId', ${item.y}, ${item.x}, ${rating})"
                )
            }
            averageRatingTextView.text = "${item.scoreAvg}점"
            averageRatingBar.rating = item.scoreAvg.toFloat()
            reviewCount.text = item.reviewCount.toString()
            comment.setText("")
            commentButton.setOnClickListener {
                UpdateContents().execute(
                    "http://$IP_ADDRESS/wheretogo/update.php",
                    "insert into review (id, lat, lon, comment) values ('$userId', ${item.y}, ${item.x}, '${comment.text}')"
                )
                comment.setText("")
                comment.clearFocus()
                UpdateComments().execute(
                    "http://$IP_ADDRESS/wheretogo/update_comments.php",
                    "select review.id, review.lat, review.lon, review.comment, review.date, user.gender, rating.rating from review, user, rating where review.lat=${item?.y} and review.lon=${item?.x} and review.id=user.id and rating.id=review.id and rating.lat=review.lat and rating.lon=rating.lon"
                )
            }
        }
    }

    private fun updateBottomSheetByItem(item: PlaceInfo) {
        UpdateComments().execute(
            "http://$IP_ADDRESS/wheretogo/update_comments.php",
            "select review.id, review.lat, review.lon, review.comment, review.date, user.gender, rating.rating from review, user, rating where review.lat=${item.y} and review.lon=${item.x} and review.id=user.id and rating.id=review.id and rating.lat=review.lat and rating.lon=rating.lon"
        )

        placeName.text = item.placeName
        placeCategory.text = categorySubString(item.categoryName)
        placeScore.text = item.scoreAvg.toString()
        placeRatingBar.rating = item.scoreAvg.toFloat()
        placeScoreCount.text = item.scoreCount.toString()
        placeReviewCount.text = item.reviewCount.toString()
        placeAddress.text = item.addressName
        placePhone.text = item.phone
        placeDistance.text = "${item.distance}m"
        if (!item.favorites) {
            favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)
        } else {
            favoritesButton.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
        }
        favoritesButton.setOnClickListener {
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
            updateBottomSheetByItem(item)
        }

        myScoreRatingBar.rating = 0f
        myScoreRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            UpdateContents().execute(
                "http://$IP_ADDRESS/wheretogo/update.php",
                "insert into rating (id, lat, lon, rating) values ('$userId', ${item.y}, ${item.x}, ${rating})"
            )
            updateBottomSheetByItem(item)
        }
        averageRatingTextView.text = "${item.scoreAvg}점"
        averageRatingBar.rating = item.scoreAvg.toFloat()
        reviewCount.text = item.reviewCount.toString()
        comment.setText("")
        commentButton.setOnClickListener {
            UpdateContents().execute(
                "http://$IP_ADDRESS/wheretogo/update.php",
                "insert into review (id, lat, lon, comment) values ('$userId', ${item.y}, ${item.x}, '${comment.text}')"
            )
            comment.setText("")
            comment.clearFocus()
            UpdateComments().execute(
                "http://$IP_ADDRESS/wheretogo/update_comments.php",
                "select review.id, review.lat, review.lon, review.comment, review.date, user.gender, rating.rating from review, user, rating where review.lat=${item?.y} and review.lon=${item?.x} and review.id=user.id and rating.id=review.id and rating.lat=review.lat and rating.lon=rating.lon"
            )
            updateBottomSheetByItem(item)
        }
    }

    private fun updatePlace(sort: String) {
        val displayCoords = getDisplayCoords()
        val topLatitude = displayCoords[0]
        val topLongitude = displayCoords[1]
        val bottomLatitude = displayCoords[2]
        val bottomLongitude = displayCoords[3]

        listViewAdapter.getPOIItemsOutOfDisplay(topLatitude, topLongitude, bottomLatitude, bottomLongitude)

        RestAPITask().execute(
            "https://dapi.kakao.com/v2/local/search/keyword.json?",
            "x=$currentX&y=$currentY&rect=$topLongitude,$topLatitude,$bottomLongitude,$bottomLatitude&page=1&size=15&sort=$sort&category_group_code=FD6&query=$keyword"
        )

        /*
        for (i in 2..page) {
            RestAPITask().execute(
                "https://dapi.kakao.com/v2/local/search/keyword.json?",
                "x=$currentX&y=$currentY&rect=$topLongitude,$topLatitude,$bottomLongitude,$bottomLatitude&page=$i&size=15&sort=$sort&category_group_code=FD6&query=$keyword"
            )
        }
         */
    }

    private fun getDisplayCoords(): DoubleArray {
        val display = windowManager.defaultDisplay  // 모바일 화면의 디스플레이 영역의 크기를 가져옴
        val init_lat = 0.0000005    // 디스플레이 크기 1당 다음지도 위도 크기 근사값
        val init_lon = 0.0000008    // 디스플레이 크기 1당 다음지도 경도 크기 근사값

        val zoomlevel = mapView.zoomLevel     // 지도 줌레벨에 따라 디스플레이 크기 1당 위도, 경도 값이 달라지니까 구함.
        val range = Math.pow(2.0, (zoomlevel + 1).toDouble())   // 지도 줌레벨에 따라 축척이 변화하므로 축척에 따른 값을 구함

        val mp = mapView.mapCenterPoint  // 다음지도 맵뷰의 가운데 값을 구함
        val gc = mp.mapPointGeoCoord  // 맵포인트를 WCONG 평면좌표계의 좌표값으로 변환

        // 중앙 좌표에서 왼쪽 상단 좌표를 구하기 위한 연산
        val top_lat = gc.latitude + (init_lat * display.height * range)
        val top_lon = gc.longitude - (init_lon * display.width * range)

        // 중앙 좌표에서 오른쪽 하단 좌표를 구하기 위한 연산
        val bottom_lat = gc.latitude - (top_lat - gc.latitude)
        val bottom_lon = gc.longitude - top_lon + gc.longitude

        return doubleArrayOf(top_lat, top_lon, bottom_lat, bottom_lon)
    }

    private fun startLocationService() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "권한 승인 필요", Toast.LENGTH_LONG).show()

            when {
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(this, "권한 승인 필요", Toast.LENGTH_LONG).show()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                    Toast.makeText(this, "권한 승인 필요", Toast.LENGTH_LONG).show()
                }
                else -> {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
                }
            }

            return
        }
    }

    class ReviewInfo(var id: String, var gender: String, var lat: Double, var lon: Double, var comment: String, var rating: Double, var date: String)

    inner class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {
        private var items = ArrayList<ReviewInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private var image: ImageView = itemView.findViewById(R.id.imageview_review_user_image)
            private var rating: RatingBar = itemView.findViewById(R.id.ratingbar_review_rating)
            private var comment: TextView = itemView.findViewById(R.id.textview_review_comment)
            private var id: TextView = itemView.findViewById(R.id.textview_review_id)
            private var date: TextView = itemView.findViewById(R.id.textview_review_date)

            fun setItem(item: ReviewInfo) {
                if (item.gender == "남자") {
                    image.setBackgroundResource(R.drawable.ic_baseline_account_circle_men_24)
                } else {
                    image.setBackgroundResource(R.drawable.ic_baseline_account_circle_women_24)
                }
                rating.rating = item.rating.toFloat()
                comment.text = item.comment
                id.text = item.id
                date.text = item.date
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.reviews, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size
        fun addItem(item: ReviewInfo) { items.add(item) }
        fun clear() { items.clear() }
    }

    inner class ListViewAdapter : RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {
        private var items = ArrayList<PlaceInfo>()

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
                    listLayout.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                    val item = items[adapterPosition]
                    updateBottomSheet(item.id.toInt())

                    mapView.setMapCenterPointAndZoomLevel(item.point, 2, true)
                    mapView.selectPOIItem(item.item, true)
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

        fun setItems(items: ArrayList<PlaceInfo>) { this.items = items }
        fun addItem(item: PlaceInfo) { items.add(item) }
        fun clearItems() {
            items.clear()
            mapView.removeAllPOIItems()
        }
        fun findItem(tag: Int): PlaceInfo? {
            for (item in items) {
                if (item.id.toInt() == tag) {
                    return item
                }
            }
            return null
        }
        fun getPOIItemsOutOfDisplay(topLatitude: Double, topLongitude: Double, bottomLatitude: Double, bottomLongitude: Double) {
            var i = 0
            while (i < items.size) {
                if (items[i].x !in topLongitude..bottomLongitude || items[i].y !in bottomLatitude..topLatitude) {
                    items[i].removeMarker()
                    items.removeAt(i)
                } else {
                    i++
                }
            }
        }
        fun sortByRating() {
            // TODO(평점 순으로 정렬)
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
    }

    inner class SearchViewAdapter : RecyclerView.Adapter<SearchViewAdapter.ViewHolder>() {
        private val items = ArrayList<SearchHistoryInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val searchString: TextView = itemView.findViewById(R.id.textview_search_string)

            init {
                itemView.setOnClickListener {
                    val item = items[adapterPosition]
                    keyword = item.word
                    keywordButton.text = keyword
                    searchButton.text = keyword
                    updatePlace(sort)
                    searchLayout.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            fun setItem(item: SearchHistoryInfo) {
                searchString.text = item.word
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.search_history, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size

        fun addItem(item: SearchHistoryInfo) {
            var flag = false
            for (temp in items) {
                if (temp.word == item.word) {
                    flag = true
                    break
                }
            }
            if (!flag) {
                items.add(item)
            }
        }
    }

    private fun getKeyword(n1: Int, n2: Int, n3: Int): String {
        val python = Python.getInstance()
        val pythonFile = python.getModule("algorithm")
        return pythonFile.callAttr("getKeyword", intArrayOf(n1, n2, n3)).toString()
    }

    inner class SelectChoice : AsyncTask<String, Void, String>() {
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
                Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val choice = item.getString("choice")

                        val n = choice.split(',')
                        keyword = getKeyword(n[0].toInt(), n[1].toInt(), n[2].toInt())
                    }
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
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
                val metaJson = jsonObject.getJSONObject("meta")
                val sameName = metaJson.getJSONObject("same_name")
                val region = sameName.get("region") // 질의어에서 인식된 지역의 리스트
                val keyword = sameName.getString("keyword") // 질의어에서 지역 정보를 제외한 키워드
                val selectedRegion = sameName.getString("selected_region")  // 인식된 지역 리스트 중, 현재 검색에 사용된 지역 정보
                val pageableCount = metaJson.getInt("pageable_count")   // total_count 중 노출 가능 문서 수
                val totalCount = metaJson.getInt("total_count") // 검색에어 검색된 문서 수
                val isEnd = metaJson.getBoolean("is_end")   // 현재 페이지가 마지막 페이지인지 여부
                val msg = region.toString() + keyword + selectedRegion + pageableCount.toString() + totalCount.toString() + isEnd.toString()

                page = pageableCount

                for (i in 0 until documentsJsonArray.length()) {
                    val documents = documentsJsonArray.getJSONObject(i)
                    val id = documents.getString("id")

                    if (listViewAdapter.findItem(id.toInt()) == null) {
                        val item = PlaceInfo(documents)
                        item.addMarker()
                        listViewAdapter.addItem(item)
                    }
                }
            } catch (e: JSONException) {
                //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
            }
            listViewAdapter.updateFavorites()
            listViewAdapter.updateRating()
            listViewAdapter.updateReview()
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

            //Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()

            listViewAdapter.updateFavorites()
            listViewAdapter.updateRating()
            listViewAdapter.updateReview()
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

                        listViewAdapter.setScoreAvg(lat, lon, avg)
                        listViewAdapter.setScoreCount(lat, lon, count)
                    }

                    listViewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
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

                        listViewAdapter.setReviewCount(lat, lon, count)
                    }

                    listViewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
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

                        listViewAdapter.setFavorites(lat, lon, true)
                    }

                    listViewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class UpdateComments : AsyncTask<String, Void, String>() {
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

            reviewViewAdapter.clear()

            if (result == "empty") {

            } else {
                try {
                    val jsonObject = JSONObject(result!!)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val id = item.getString("id")
                        val lat = item.getDouble("lat")
                        val lon = item.getDouble("lon")
                        val comment = item.getString("comment")
                        val date = item.getString("date")
                        val gender = item.getString("gender")
                        val rating = item.getDouble("rating")

                        reviewViewAdapter.addItem(ReviewInfo(id, gender, lat, lon, comment, rating, date))
                    }
                } catch (e: JSONException) {
                    //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            reviewViewAdapter.notifyDataSetChanged()
            listViewAdapter.updateFavorites()
            listViewAdapter.updateRating()
            listViewAdapter.updateReview()
        }
    }

    inner class UpdateSearchHistory : AsyncTask<String, Void, String>() {
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
                        val id = item.getString("id")
                        val date = item.getString("date")
                        val word = item.getString("word")

                        searchViewAdapter.addItem(SearchHistoryInfo(date, word))
                    }

                    searchViewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    //Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    class SearchHistoryInfo(var date: String, var word: String)

    override fun onCallbackFromFavorites(item: PlaceInfo, items: ArrayList<PlaceInfo>) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        listViewAdapter.setItems(items)
        updateBottomSheetByItem(item)

        mapView.setMapCenterPointAndZoomLevel(item.point, 2, true)
        mapView.selectPOIItem(item.item, true)
    }

    override fun onCallbackFromFavoritesMarker(item: PlaceInfo, items: ArrayList<PlaceInfo>) {
        listViewAdapter.setItems(items)
        updateBottomSheetByItem(item)
    }
}

/*
val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
if (location != null) {
    Toast.makeText(applicationContext, "${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
}

val gpsListener = GPSListener(applicationContext)

manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 0F, gpsListener)

class GPSListener(private val context: Context) : LocationListener {
    override fun onLocationChanged(location: Location?) {
        val latitude = location?.latitude
        val longitude = location?.longitude
        Toast.makeText(context, "이거란말이야 ${location?.latitude}, ${location?.longitude}", Toast.LENGTH_SHORT).show()
    }
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}
}
 */

// App Hash Key
fun getSignature(context: Context): String? {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)

    for (signature: Signature in packageInfo.signatures) {
        val messageDigest = MessageDigest.getInstance("SHA")
        messageDigest.update(signature.toByteArray())
        return Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT)
    }

    return null
}