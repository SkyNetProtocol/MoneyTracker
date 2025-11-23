package com.example.loginapp.presentation.summary

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SummaryPagerAdapter(
    fragment: Fragment,
    private val userId: Int
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CategorySummaryFragment.newInstance(userId, "PERSONAL")
            1 -> CategorySummaryFragment.newInstance(userId, "COMPANY")
            else -> CategorySummaryFragment.newInstance(userId, "PERSONAL")
        }
    }
}
