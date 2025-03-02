package com.airprint.smart.printer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.OnBoardingViewpagerAdapter
import com.airprint.smart.printer.ads.NativeAdManager
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityOnBoardingBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.ControlConsent
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.setSafeOnClickListener

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    override fun setupViewBinding(): ActivityOnBoardingBinding {
        return ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decideTheme()
        nativeAds()
        inItViews()
    }

    private fun inItViews() {
        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = OnBoardingViewpagerAdapter(this, 2)
        binding.tick.setSafeOnClickListener {
            if (binding.viewPager.currentItem >= 1) {
                goToLanguage()
            } else {
                binding.viewPager.currentItem += 1
            }
            when (binding.viewPager.currentItem) {
                0 -> {
                    binding.layoutTop.visibility= View.VISIBLE
                    binding.layoutTop1.visibility= View.GONE

                }
                1 -> {
                    binding.layoutTop.visibility= View.GONE
                    binding.layoutTop1.visibility= View.VISIBLE
                }

            }
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.layoutTop.visibility = View.VISIBLE
                        binding.layoutTop1.visibility = View.GONE
                        binding.tick.setImageResource(R.drawable.first_half)

                    }
                    1 -> {
                        binding.layoutTop.visibility = View.GONE
                        binding.layoutTop1.visibility = View.VISIBLE
                        binding.tick.setImageResource(R.drawable.start_button)

                    }

                }
            }
        })
        binding.tvSkip.setSafeOnClickListener {
            goToLanguage()
        }
    }

    private fun goToLanguage() {
        SharedPreferenceHelper.setBoolean(this@OnBoardingActivity,Constent.IS_INTRO_VISIT,true)
        launchActivity<LanguageActivity> ()
        finish()
    }

    private fun nativeAds() {
        if (/*SharedPreferenceHelper.getBoolean(this, ControlConsent.INTRO_NATIVE_CONTROL) &&*/ isNetworkAvailable()) {
            val adUnit = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                SharedPreferenceHelper.getString(this@OnBoardingActivity, IdesConstent.INTRO_NATIVE_ID)
            }
            adUnit?.let {
                NativeAdManager.requestAdmobNativeAd(this,
                    it,
                    binding.loadNative.flAdPlaceholder,
                    binding.loadNative.loadingCard,Constent.SMALL_WITH_MEDIA

                )
            }
        } else {
            binding.loadNative.loadingCard.visibility = View.GONE
        }
    }

}