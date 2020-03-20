package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.rpc.Help


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var fullView : DrawerLayout
    lateinit var activityContainer : FrameLayout
    lateinit var fragmentContainer : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {

        fullView = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        activityContainer = fullView.findViewById<View>(R.id.activity_content) as FrameLayout
        layoutInflater.inflate(layoutResID, activityContainer, true)

       // fragmentContainer = fullView.findViewById<View>(R.id.fragment_container) as FrameLayout


        super.setContentView(fullView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = resources.getString(R.string.app_name)

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val mDrawerToggle = ActionBarDrawerToggle(this, fullView, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        fullView.addDrawerListener(mDrawerToggle)
        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerToggle.syncState()



    }

   override fun onNavigationItemSelected(item: MenuItem): Boolean {

        var selectedFragment = Fragment()
        val itemId = item.itemId

       if ( itemId == R.id.menu_sign_out) {
           val intent = Intent(this, MainActivity::class.java)
           startActivity(intent)
       }

       else {
           when(itemId) {
               R.id.menu_profile -> selectedFragment = ProfileFragment()
               R.id.menu_history -> selectedFragment = HistoryFragment()
               R.id.menu_howtoride -> selectedFragment = HowToRideFragment()
               R.id.menu_help -> selectedFragment = HelpFragment()
           }


           activityContainer.visibility = View.GONE

           supportFragmentManager.beginTransaction().replace(R.id.fragment, selectedFragment).commit()

       }

        fullView.closeDrawers()
        return true
    }

}
