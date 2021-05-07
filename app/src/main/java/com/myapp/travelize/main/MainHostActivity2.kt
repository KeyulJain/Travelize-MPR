package com.myapp.travelize.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myapp.travelize.Constants.Companion.ACTION_CHAT_GROUP_SELECTED
import com.myapp.travelize.Constants.Companion.ACTION_KEY
import com.myapp.travelize.R
import com.myapp.travelize.authentication.MainActivity.Companion.FIRESTORE_SHARED_PREF
import com.myapp.travelize.authentication.MainActivity.Companion.USER_EMAIL
import com.myapp.travelize.authentication.MainActivity.Companion.USER_NAME
import com.myapp.travelize.interfaces.FragmentActionListener
import com.myapp.travelize.main.MainHostActivity.Companion.PROFILE_PIC_URL
import com.myapp.travelize.main.MainHostActivity.Companion.USER_DOB
import com.myapp.travelize.main.MainHostActivity.Companion.USER_GENDER
import com.myapp.travelize.main.MainHostActivity.Companion.USER_INSTITUTE_NAME
import com.myapp.travelize.main.MainHostActivity.Companion.USER_PASSIONS
import com.myapp.travelize.main.mainscreen.ChatFragment
import com.myapp.travelize.main.mainscreen.ChatHostFragment
import com.myapp.travelize.main.mainscreen.HomeFragment
import com.myapp.travelize.main.mainscreen.ProfileFragment
import com.myapp.travelize.models.User


class MainHostActivity2 : AppCompatActivity(),FragmentActionListener {
    companion object {
        const val USER_LAT = "latitude"
        const val USER_LONG = "longitude"
        const val TYPE_RESTAURANT = "restaurant"
        const val KEYWORD_RESTAURANT = "food"
        const val TYPE_MALL = "shopping_mall"
        const val KEYWORD_MALL = "shop"
        const val TYPE_THEATER = "movie_theater"
        const val KEYWORD_THEATER = "movie"
        const val TYPE_PARK = "park"
        const val KEYWORD_PARK = "park"
        const val HOME_FRAGMENT_TAG = "home"
        const val CHAT_HOST_FRAGMENT_TAG = "chat_host"
        const val PROFILE_FRAGMENT_TAG = "profile"
        const val CHAT_FRAGMENT_TAG = "chat"
        const val CHAT_GROUP_KEY="chat_group_key"
        const val CHAT_BACKSTACK="chat_backstack"
    }

    private val requestAccessFineLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.e("Location Permission", "Granted")
            } else {
                Log.e("Location Permission", "Denied")
            }
        }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val collectionRef = db.collection("Users")
    lateinit var fragmentManager: FragmentManager
    lateinit var bottomNavigationView: BottomNavigationView
    val docRef = collectionRef.document(auth.getCurrentUser().getUid())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host2)
        supportActionBar?.hide()
        fragmentManager = supportFragmentManager

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        val i = intent
        val isNewUser = i.getBooleanExtra("New User", false)
        Log.e("Info", "MainHostActivity2 and New User Status:$isNewUser")
        if (isNewUser) {
            createUserProfile()
        }
        if (!hasAccessFineLocationPermission()) {
            requestAccessFineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            val homeFragment = fragmentManager.findFragmentByTag(HOME_FRAGMENT_TAG) as? HomeFragment
            Log.e("homeFragment check", homeFragment.toString())
            val chatFragment= fragmentManager.findFragmentByTag(CHAT_HOST_FRAGMENT_TAG) as? ChatHostFragment
            Log.e("chatFragment check", chatFragment.toString())
            val profileFragment = fragmentManager.findFragmentByTag(PROFILE_FRAGMENT_TAG) as? ProfileFragment
            Log.e("profileFragment check", profileFragment.toString())

            when (it.itemId) {
                R.id.item_home -> {
                    Log.e("HomeItem", "clicked")
                    if (homeFragment != null && !homeFragment.isVisible) {
                        fragmentManager.beginTransaction().show(homeFragment).commit()
                    }
                    if (chatFragment != null && chatFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(chatFragment).commit()
                    }
                    if (profileFragment != null && profileFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(profileFragment).commit()
                    }
                }

                R.id.item_chat -> {
                    Log.e("ChatItem", "clicked")
                    if (chatFragment != null && !chatFragment.isVisible) {
                        fragmentManager.beginTransaction().show(chatFragment).commit()
                    } else if(chatFragment==null) {
                        val chatHostFragment=ChatHostFragment()
                        chatHostFragment.fragmentActionListener=this
                        fragmentManager
                            .beginTransaction()
                            .add(R.id.main_fragment_container2,chatHostFragment, CHAT_HOST_FRAGMENT_TAG)
                            .commit()
                        Log.e("Info", "new chat fragment created!")
                    }
                    if (homeFragment != null && homeFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(homeFragment).commit()
                    }
                    if (profileFragment != null && profileFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(profileFragment).commit()
                    }
                }

                R.id.item_profile -> {
                    Log.e("ProfileItem", "clicked")
                    if (profileFragment != null && !profileFragment.isVisible) {
                        fragmentManager.beginTransaction().show(profileFragment).commit()
                    } else if(profileFragment==null) {
                        val profFragment=ProfileFragment()
                        fragmentManager.beginTransaction()
                            .add(R.id.main_fragment_container2, profFragment, PROFILE_FRAGMENT_TAG)
                            .commit()
                        Log.e("Info", "new profile fragment created!")
                    }
                    if (homeFragment != null && homeFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(homeFragment).commit()
                    }
                    if (chatFragment != null && chatFragment.isVisible) {
                        fragmentManager.beginTransaction().hide(chatFragment).commit()
                    }
                }
            }
            true
        }
    }

    private fun createUserProfile() {
        val sharedPref = getSharedPreferences(FIRESTORE_SHARED_PREF, MODE_PRIVATE)
        val name = sharedPref.getString(USER_NAME, null)
        val email = sharedPref.getString(USER_EMAIL, null)
        val dob = sharedPref.getString(USER_DOB, null)
        val gender = sharedPref.getString(USER_GENDER, null)
        val tempSet: Set<String> =
            HashSet<String>(sharedPref.getStringSet(USER_PASSIONS, HashSet<String>()))
        val passions: List<String> = tempSet.toList()
        val imageURL = sharedPref.getString(PROFILE_PIC_URL, null)
        val institute = sharedPref.getString(USER_INSTITUTE_NAME, null)

        val user = User(
            name,
            email,
            dob,
            gender,
            institute,
            passions,
            imageURL,
            null,
            null,
            null,
            null,
            null
        )

        docRef
            .set(user).addOnSuccessListener {
                Log.e("MainHostActivity2", "DocumentSnapshot successfully written!");
            }
            .addOnFailureListener {
                it.printStackTrace()
                Log.e("MainHostActivity2", "Failure to write document")
            }
    }

    fun hasAccessFineLocationPermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun saveUserLocation(latitude: Double, longitude: Double) {
        val sharedPref = getSharedPreferences(FIRESTORE_SHARED_PREF, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(USER_LAT, latitude.toString())
        editor.putString(USER_LONG, longitude.toString())
        editor.apply()
        Log.e("All prefs", sharedPref.all.toString())
    }

    private fun addChatFragment(bundle: Bundle) {
        val chatFragment=ChatFragment()
        chatFragment.arguments=bundle
        bottomNavigationView.visibility=GONE
        fragmentManager
            .beginTransaction()
            .replace(R.id.main_fragment_container2,chatFragment, CHAT_FRAGMENT_TAG)
            .addToBackStack(CHAT_BACKSTACK)
            .commit()
    }

    override fun onActionCallBack(bundle: Bundle) {
        Log.e("onActionCallBack","called!")
        val actionPerformed = bundle.getInt(ACTION_KEY)

        when (actionPerformed) {
            ACTION_CHAT_GROUP_SELECTED -> {
                addChatFragment(bundle)
            }
        }
    }

    override fun onBackPressed() {
        if(this::fragmentManager.isInitialized && fragmentManager.findFragmentById(R.id.main_fragment_container2) is ChatFragment)
        {
            bottomNavigationView.visibility= VISIBLE
        }
        super.onBackPressed()
    }
}
