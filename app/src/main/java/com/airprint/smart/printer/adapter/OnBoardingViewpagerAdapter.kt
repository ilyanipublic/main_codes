package com.airprint.smart.printer.adapter

import android.os.Bundle
import androidx.compose.ui.unit.Constraints
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.airprint.smart.printer.ui.fragments.OnBoardingFragment
import com.airprint.smart.printer.utils.Constent

class OnBoardingViewpagerAdapter(fragmentActivity: FragmentActivity, size: Int) :
    FragmentStateAdapter(fragmentActivity) {
    private var pageSize = size

    override fun getItemCount(): Int {
        return pageSize
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt(Constent.ARG_POSITION_ADP, position)
        val fragment= OnBoardingFragment()
        fragment.arguments = bundle

        return fragment
    }
    fun updatePosition(newPageSize: Int) {
        pageSize = newPageSize
        notifyDataSetChanged()
    }
}