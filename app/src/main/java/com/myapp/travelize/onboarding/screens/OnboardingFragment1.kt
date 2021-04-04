package com.myapp.travelize.onboarding.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.myapp.travelize.authentication.MainActivity
import com.myapp.travelize.R
import com.myapp.travelize.onboarding.ViewPagerActivity

class OnboardingFragment1 : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_onboarding1, container, false)
        val nextBtn1 = view.findViewById<Button>(R.id.onboardingBtn1)
        val skipBtn1 = view.findViewById<Button>(R.id.skipBtn1)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboardingViewPager)

        nextBtn1.setOnClickListener {
            viewPager?.currentItem = 1
        }

        skipBtn1.setOnClickListener {
            val sharedPref = activity?.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()
            editor?.putBoolean("finished", true)
            editor?.apply()

            val intent = Intent(activity, MainActivity::class.java)
            val parentActivity = activity as? ViewPagerActivity
            Log.e("Result of safe cast", parentActivity.toString())
            parentActivity?.startActivity(intent)
            parentActivity?.finish()
        }
        return view
    }
}