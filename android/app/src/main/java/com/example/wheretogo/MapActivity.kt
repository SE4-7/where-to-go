package com.example.wheretogo

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import net.daum.mf.map.api.MapView
import java.security.MessageDigest

class MapActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        drawerLayout = findViewById(R.id.drawer_layout)

        val mapView = MapView(this)
        val mapViewContainer: ViewGroup = findViewById(R.id.mapView)
        mapViewContainer.addView(mapView)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
