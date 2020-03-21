package com.example.android_project_bike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.findFragment
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.rpc.Help
import kotlinx.android.synthetic.main.activity_base.*
import kotlin.reflect.typeOf


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var fullView : DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navigationMenu: Menu
    private lateinit var activityContainer : FrameLayout
    private lateinit var fragmentContainer : FrameLayout
    private lateinit var fragment : Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    override fun setContentView(layoutResID: Int) {

        fullView = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        activityContainer = fullView.findViewById<View>(R.id.activity_content) as FrameLayout
        layoutInflater.inflate(layoutResID, activityContainer, true)

       fragmentContainer = fullView.findViewById<View>(R.id.fragment_container) as FrameLayout
       fragment = supportFragmentManager.findFragmentById(R.id.fragment)!!


        super.setContentView(fullView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = resources.getString(R.string.app_name)

        navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val mDrawerToggle = ActionBarDrawerToggle(this, fullView, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        fullView.addDrawerListener(mDrawerToggle)
        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerToggle.syncState()

    }

    override fun onStart() {
        super.onStart()

        auth.addAuthStateListener {

            navigationMenu = navigationView.getMenu()

            if (auth.currentUser != null) {
                navigationMenu.findItem(R.id.menu_profile).setVisible(true)
                navigationMenu.findItem(R.id.menu_history).setVisible(true)
                navigationMenu.findItem(R.id.menu_sign_out).setVisible(true)
                navigationMenu.findItem(R.id.menu_sign_in).setVisible(false)
            }

            else {
                navigationMenu.findItem(R.id.menu_profile).setVisible(false)
                navigationMenu.findItem(R.id.menu_history).setVisible(false)
                navigationMenu.findItem(R.id.menu_sign_out).setVisible(false)
                navigationMenu.findItem(R.id.menu_sign_in).setVisible(true)
            }

        }
    }

    override fun onBackPressed() {

        if (activityContainer.visibility == View.GONE) {
            activityContainer.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
        }
        else {
            super.onBackPressed()
        }
    }


   override fun onNavigationItemSelected(item: MenuItem): Boolean {

        var selectedFragment = Fragment()
        val itemId = item.itemId
       auth = FirebaseAuth.getInstance()


       if ( itemId == R.id.menu_sign_in) {
           val intent = Intent(this, LoginActivity::class.java)
           startActivity(intent)
       }

       else if ( itemId == R.id.menu_sign_out) {
           auth.signOut()
           val intent = Intent(this, MainActivity::class.java)
           startActivity(intent)
           finish()
       }
       else {
           when(itemId) {
               R.id.menu_profile -> selectedFragment = ProfileFragment()
               R.id.menu_history -> selectedFragment = HistoryFragment()
               R.id.menu_howtoride -> selectedFragment = HowToRideFragment()
               R.id.menu_help -> selectedFragment = HelpFragment()
           }


           activityContainer.visibility = View.GONE
           fragmentContainer.visibility = View.VISIBLE

           supportFragmentManager.beginTransaction().replace(R.id.fragment, selectedFragment).commit()

       }

        fullView.closeDrawers()
        return true
    }

}
