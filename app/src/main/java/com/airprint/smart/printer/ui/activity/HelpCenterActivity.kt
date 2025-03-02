package com.airprint.smart.printer.ui.activity

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.HelpViewPagerAdapter
import com.airprint.smart.printer.databinding.ActivityHelpCenterBinding
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HelpCenterActivity : BaseActivity<ActivityHelpCenterBinding>() {
    override fun setupViewBinding(): ActivityHelpCenterBinding {
        return ActivityHelpCenterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.post {
            decideTheme()
            inItView()
        }

    }

    private fun inItView() {
        setUpViewPager()
        binding.continueBtn.setSafeOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < 2) {
                binding.continueBtn.text=getString(R.string.cont)
                binding.viewPager.currentItem = currentItem + 1
            } else {
                binding.continueBtn.text=getString(R.string.done)
                Toast.makeText(this, "Reached the last screen", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backBtn.setSafeOnClickListener {
            finish()
        }
    }
    private fun setUpViewPager() {
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = HelpViewPagerAdapter(this, 3)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0) {
                updateTabIconColor(tab, true)
            } else {
                updateTabIconColor(tab, false)
            }
            updateTabIconColor(tab, true)
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTabIconColor(tab, true)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                updateTabIconColor(tab, false)
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.viewPager.isUserInputEnabled = true
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position==2){
                    binding.continueBtn.text=getString(R.string.done)
                }else{
                    binding.continueBtn.text=getString(R.string.cont)
                }


            }
        })
    }

    private fun updateTabIconColor(tab: TabLayout.Tab?, isSelected: Boolean) {
        val icon = tab?.icon
        if (icon is Drawable) {
            val color = if (isSelected) {
                ContextCompat.getColor(this, R.color.button_color)
            } else {
                ContextCompat.getColor(this, R.color.tab_un_selected_color)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                icon.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            } else
                icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }


}