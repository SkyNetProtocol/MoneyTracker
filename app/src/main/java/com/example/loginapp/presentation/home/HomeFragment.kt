package com.example.loginapp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.loginapp.R
import com.example.loginapp.databinding.FragmentHomeBinding
import com.example.loginapp.presentation.moneytracker.MoneyTrackerFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val args: HomeFragmentArgs by navArgs()
    
    private var currentCategory = "PERSONAL"
    private var pagerAdapter: HomePagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get userId and category from SafeArgs
        val userId = args.userId
        currentCategory = args.category

        pagerAdapter = HomePagerAdapter(this, userId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Money Tracker"
                1 -> "Analytics"
                2 -> "Graph"
                3 -> "Calendar"
                else -> "Tab ${position + 1}"
            }
        }.attach()

        // Set bottom navigation to match the category
        binding.bottomNavigation.selectedItemId = when (currentCategory) {
            "COMPANY" -> R.id.nav_company
            else -> R.id.nav_personal
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_personal -> {
                    currentCategory = "PERSONAL"
                    refreshFragments()
                    true
                }
                R.id.nav_company -> {
                    currentCategory = "COMPANY"
                    refreshFragments()
                    true
                }
                else -> false
            }
        }
    }

    private fun refreshFragments() {
        pagerAdapter?.notifyDataSetChanged()
        // Force ViewPager to recreate fragments
        binding.viewPager.adapter = null
        binding.viewPager.adapter = pagerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private inner class HomePagerAdapter(fragment: Fragment, private val userId: Int) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    MoneyTrackerFragment().apply {
                        arguments = Bundle().apply {
                            putInt("USER_ID", userId)
                            putString("CATEGORY", currentCategory)
                        }
                    }
                }
                1 -> {
                    com.example.loginapp.presentation.analytics.AnalyticsFragment().apply {
                        arguments = Bundle().apply {
                            putInt("USER_ID", userId)
                            putString("CATEGORY", currentCategory)
                        }
                    }
                }
                2 -> {
                    com.example.loginapp.presentation.graph.GraphFragment().apply {
                        arguments = Bundle().apply {
                            putInt("USER_ID", userId)
                            putString("CATEGORY", currentCategory)
                        }
                    }
                }
                3 -> {
                    com.example.loginapp.presentation.calendar.CalendarFragment().apply {
                        arguments = Bundle().apply {
                            putInt("USER_ID", userId)
                            putString("CATEGORY", currentCategory)
                        }
                    }
                }
                else -> Fragment()
            }
        }
    }
}
