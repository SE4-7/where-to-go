package com.example.wheretogo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.daum.mf.map.api.MapView
import java.security.MessageDigest

class MapActivity : AppCompatActivity(), View.OnClickListener {
    // For Main Layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: Button
    private lateinit var searchButton: Button
    private lateinit var zoomInButton: Button
    private lateinit var zoomOutButton: Button
    private lateinit var myLocationButton: Button
    private lateinit var listButton: FloatingActionButton

    // For List Layout
    private lateinit var listLayout: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var mapButton: FloatingActionButton

    // For Search Layout
    private lateinit var searchLayout: FrameLayout
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        drawerLayout = findViewById(R.id.drawer_layout)
        menuButton = findViewById(R.id.button_menu)
        searchButton = findViewById(R.id.button_search)
        zoomInButton = findViewById(R.id.button_zoom_in)
        zoomOutButton = findViewById(R.id.button_zoom_out)
        myLocationButton = findViewById(R.id.button_my_location)
        listButton = findViewById(R.id.button_list)

        listLayout = findViewById(R.id.layout_list)
        listLayout.visibility = View.GONE
        recyclerView = findViewById(R.id.recyclerview_list)
        mapButton = findViewById(R.id.button_map)

        searchLayout = findViewById(R.id.layout_search)
        searchLayout.visibility = View.GONE
        backButton = findViewById(R.id.button_back)

        setButtonClickListener()

        val mapView = MapView(this)
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
            R.id.button_zoom_in -> {
                // TODO("지도 확대")
            }
            R.id.button_zoom_out -> {
                // TODO("지도 축소")
            }
            R.id.button_my_location -> {
                // TODO("내 위치로 이동")
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