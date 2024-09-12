package edu.linus.apitest;

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class NavFragment : Fragment(R.layout.fragment_nav), NavigationBarView.OnItemSelectedListener {
    var nav: BottomNavigationView? = null

    override fun onViewCreated(createdView: View, savedInstanceState: Bundle?) {
        nav = createdView.findViewById(R.id.bottomNavigationView)

//        //Check if the activity is instance of one of the two, and if so set it to be active item.
//        if (activity is HomeActivity) {
//            nav?.setSelectedItemId(R.id.homeNav);
//        } else if (activity is FavoritesActivity) {
//            nav?.setSelectedItemId(R.id.favoritesNav);
//        } else if (activity is SearchActivity) {
//            nav?.setSelectedItemId(R.id.searchNav);
//        }
//
//        //Set the listener AFTER the active item is changed
//        nav?.setOnItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (activity is HomeActivity) {
            nav?.setSelectedItemId(R.id.homeNav);
        } else if (activity is FavoritesActivity) {
            nav?.setSelectedItemId(R.id.favoritesNav);
        } else if (activity is SearchActivity) {
            nav?.setSelectedItemId(R.id.searchNav);
        }
        nav?.setOnItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.homeNav -> {
                this.startActivity(Intent(context, HomeActivity::class.java))
                return true
            }
            R.id.favoritesNav -> {
                this.startActivity(Intent(context, FavoritesActivity::class.java))
                return true
            }
            R.id.searchNav -> {
                this.startActivity(Intent(context, SearchActivity::class.java))
                return true
            }
            R.id.logoutNav -> {
                //Logout
                val sharedPref = context?.getSharedPreferences(getString(R.string.storage_key), Context.MODE_PRIVATE) ?: return false

                with(sharedPref.edit()) {
                    putString(getString(R.string.storage_key), null)
                    apply()
                }
                val intent: Intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) //Clear backstack
                startActivity(intent)
                return true
            }
            else -> {
                return false
            }
        }
    }
}