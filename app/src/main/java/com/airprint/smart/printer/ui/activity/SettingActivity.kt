package com.airprint.smart.printer.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivitySettingBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.dialogPrivacy
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.moreApp
import com.airprint.smart.printer.utils.rateUsApp
import com.airprint.smart.printer.utils.setNightMode
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.airprint.smart.printer.utils.setUpAnimation
import com.airprint.smart.printer.utils.shareOurApp

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun setupViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      binding.root.post {
          decideTheme()
          initView()
      }
    }

    private fun initView() {
        listener()
    }

    private fun listener() {
        binding.apply {
            val isDarkTheme = SharedPreferenceHelper.getBoolean(this@SettingActivity, Constent.THEME_MOD, false)
            switchTheme.isChecked = isDarkTheme
            switchTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    SharedPreferenceHelper.setBoolean(this@SettingActivity,Constent.THEME_MOD,true)
                    setNightMode(true)
                }else{
                    SharedPreferenceHelper.setBoolean(this@SettingActivity,Constent.THEME_MOD,false)
                    setNightMode(false)
                }
            }
            rateUs.setSafeOnClickListener {
                rateUsApp()
            }
            shareApp.setSafeOnClickListener {
                shareOurApp()
            }
            moreApps.setSafeOnClickListener {
                moreApp()
            }
            pPolicy.setSafeOnClickListener {
                dialogPrivacy()
            }
            backBtn.setSafeOnClickListener {
                finish()
            }
            appCompatImageView2.setSafeOnClickListener {
                launchActivity<LanguageActivity>()
            }
            proBtn.setSafeOnClickListener {
                launchActivity<PremiumActivity>()
            }
            setUpAnimation(cardView)
        }
    }
}