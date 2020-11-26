package com.example.wheretogo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView
import java.security.MessageDigest

class MapActivity : AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, MapView.CurrentLocationEventListener {
    // For Main Layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: Button
    private lateinit var searchButton: Button
    private lateinit var zoomInButton: Button
    private lateinit var zoomOutButton: Button
    private lateinit var myLocationButton: Button
    private lateinit var listButton: FloatingActionButton

    // For changing fragment on navigation view
    private lateinit var tempFragment: Fragment
    private lateinit var fragmentContainer: FrameLayout

    // For List Layout
    private lateinit var listLayout: FrameLayout
    private lateinit var mapButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var listViewAdapter: ListViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    // For Search Layout
    private lateinit var searchLayout: FrameLayout
    private lateinit var backButton: Button

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        tempFragment = TempFragment()
        fragmentContainer = findViewById(R.id.container_fragment)

        menuButton = findViewById(R.id.button_menu)
        searchButton = findViewById(R.id.button_search)
        zoomInButton = findViewById(R.id.button_zoom_in)
        zoomOutButton = findViewById(R.id.button_zoom_out)
        myLocationButton = findViewById(R.id.button_my_location)
        listButton = findViewById(R.id.button_list)

        // For List Layout
        listLayout = findViewById(R.id.layout_list)
        listLayout.visibility = View.GONE
        viewManager = LinearLayoutManager(applicationContext)
        listViewAdapter = ListViewAdapter()
        for (i in 0 until 10) {
            listViewAdapter.addItem(ListItem("Name$i"))
        }
        recyclerView = findViewById(R.id.recyclerview_list)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = listViewAdapter
        }
        mapButton = findViewById(R.id.button_map)

        // For Search Layout
        searchLayout = findViewById(R.id.layout_search)
        searchLayout.visibility = View.GONE
        backButton = findViewById(R.id.button_back)

        setButtonClickListener()

        mapView = MapView(this)
        mapView.setCurrentLocationEventListener(this)

        // ì¤‘ì‹¬?  ë³?ê²?
        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);

        // ì¤? ? ˆë²? ë³?ê²?
        //mapView.setZoomLevel(7, true);

        // ì¤‘ì‹¬?  ë³?ê²? + ì¤? ? ˆë²? ë³?ê²?
        //mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);

        // ì¤? ?¸
        //mapView.zoomIn(true);

        // ì¤? ?•„?›ƒ
        //mapView.zoomOut(true);
        val mapViewContainer: ViewGroup = findViewById(R.id.mapView)
        mapViewContainer.addView(mapView)
    }

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
            }
            R.id.button_zoom_in -> { mapView.zoomIn(true) }
            R.id.button_zoom_out -> {
                mapView.zoomOut(true);
            }
            R.id.button_my_location -> {
                // TODO("?‚´ ?œ„ì¹˜ë¡œ ?´?™")
            }
            R.id.button_list -> {
                listLayout.visibility = View.VISIBLE
            }
            R.id.button_map -> {
                listLayout.visibility = View.GONE
            }
            R.id.button_back -> {
                searchLayout.visibility = View.GONE
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu1 -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, tempFragment).commit()
                fragmentContainer.visibility = View.VISIBLE
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        TODO("Not yet implemented")
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        TODO("Not yet implemented")
    }
}

class ListViewAdapter : RecyclerView.Adapter<ListViewAdapter.ViewHolder>() {
    private val items = ArrayList<ListItem>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var name: TextView = itemView.findViewById(R.id.textview_name)

        fun setItem(item: ListItem) {
            name.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_list, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ListItem) { items.add(item) }
}

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