package com.example.prototype.activity

import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.prototype.database.FeedReaderDbHelper
import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import com.example.prototype.fragments.DashboardFragment
import com.example.prototype.fragments.HomeFragment
import com.example.prototype.fragments.SettingsFragment

//key to value
const val SELECTED_TEAM_MESSAGE = "com.example.prototype.selected_team_MESSAGE"

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DbQueryHelper.initializeDB(applicationContext)

        initializeBottomNavigation()

        openFragmentWithNoBack(HomeFragment.newInstance("",""))
    }

    override fun onDestroy() {
        DbQueryHelper.closeDB()
        super.onDestroy()
    }

    private fun openFragmentWithNoBack(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction() as FragmentTransaction
        transaction.replace(R.id.fragment_container, fragment)
        //do not add the empty activity page to the stack
        //transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openFragment(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction() as FragmentTransaction
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initializeBottomNavigation()
    {
        val bottomNavigationView = findViewById(R.id.bottomNavigationView) as BottomNavigationView

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    openFragment(HomeFragment.newInstance("", ""))
                }
                R.id.navigation_play -> {
                    openFragment(DashboardFragment.newInstance("", ""))
                }

                R.id.navigation_settings -> {
                    openFragment(SettingsFragment.newInstance("", ""))
                }
            }
            true
        }
    }
}
