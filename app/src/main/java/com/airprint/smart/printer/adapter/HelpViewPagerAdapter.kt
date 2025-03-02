package com.airprint.smart.printer.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.airprint.smart.printer.ui.fragments.HelpFirstFragment
import com.airprint.smart.printer.ui.fragments.HelpSecondFragment
import com.airprint.smart.printer.ui.fragments.HelpThirdFragment
import com.airprint.smart.printer.utils.Constent

class HelpViewPagerAdapter(fragmentActivity: FragmentActivity, size: Int) :
    FragmentStateAdapter(fragmentActivity) {
    private var pageSize = size
    override fun getItemCount(): Int {
        return pageSize
    }
    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt(Constent.ARG_POSITION, position)
        return when (position) {
            0 -> {
                val fragment = HelpFirstFragment()
                fragment.arguments = bundle
                fragment
            }
            1 -> {
                val fragment = HelpSecondFragment()
                fragment.arguments = bundle
                fragment
            }
            2 -> {
                val fragment = HelpThirdFragment()
                fragment.arguments = bundle
                fragment
            }
            else -> {
                val fragment = HelpFirstFragment()
                fragment.arguments = bundle
                fragment
            }
        }
    }
}
