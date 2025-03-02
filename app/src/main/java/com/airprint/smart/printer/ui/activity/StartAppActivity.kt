package com.airprint.smart.printer.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.RemoteConfigCallBack
import com.airprint.smart.printer.ads.RemoteConfigHelper
import com.airprint.smart.printer.databinding.ActivityStartAppBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.utils.GoogleMobileAdsConsentManager
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.decideThemeSpecial
import com.airprint.smart.printer.utils.isAppStatusBarLightForWhiteColorForSplash
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.setNightMode
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.FormError
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class StartAppActivity : BaseActivity<ActivityStartAppBinding>(), RemoteConfigCallBack {
    private var progressAnimator: ObjectAnimator? = null
    private var goNextOnAnimEnd = false
    private var showAdLoadingView = false
    private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var isAppInBackground: Boolean = false
    private var myCTR = 0
    override fun setupViewBinding(): ActivityStartAppBinding {
        return ActivityStartAppBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        initViews()

    }

    private fun initViews() {
        checkAllPremium()
        inIts()
    }

    private fun checkAllPremium() {
     Constent.isInterstitialAdShowing=true
    }


    private fun inIts() {
        Log.d(TAG, "initView(): outer part is run")
        if (isNetworkAvailable()) {
            fireLogEvent("splash_launch_with_internet_call", "Splash_Activity", "Call")
            if (SharedPreferenceHelper.getBoolean(this, Constent.IS_CONSENT_KEY, false)) {
                animateProgressBar(0, 66, 3000)
            } else {
                animateProgressBar(0, 33, 2000)
            }
            binding.progressTv.text = getString(R.string.fetch_settings)
            fireLogEvent("splash_remote_call", "Splash_Activity", "Call")
            RemoteConfigHelper.fireBaseRemoteFetch(this@StartAppActivity, this@StartAppActivity)
        } else {
            fireLogEvent("splash_launch_without_internet_call", "Splash_Activity", "Call")
            fireLogEvent("splash_no_internet", "Splash_Activity", "Call")
            goNextOnAnimEnd = true
            animateProgressBar(0, 100, 1000)
        }
    }

    override fun onRemoteSuccess() {
        fireLogEvent("splash_remote_success", "Splash Activity", "Call")
        callForceUpdate()
    }

    override fun onRemoteFailed() {
        fireLogEvent("splash_remote_failed", "Splash Activity", "Call")
        callForceUpdate()
    }

    private fun goToNext() {
        if (SharedPreferenceHelper.getBoolean(this@StartAppActivity, Constent.IS_INTRO_VISIT)) {
            launchActivity<MainActivity>()
        } else {
            launchActivity<OnBoardingActivity>()
        }
        finish()
    }

    private fun callForceUpdate() {
        Log.d(
            TAG,
            "callForceUpdate: ${SharedPreferenceHelper.getLong(this, Constent.VERSION_CODES)}"
        )
        if (BuildConfig.VERSION_CODE < SharedPreferenceHelper.getLong(
                this,
                Constent.VERSION_CODES
            )
        ) {
            binding.root.visibility = View.INVISIBLE
            showUpdateAppDialog()
        } else {
            proceedFurther()
        }
    }

    private fun showUpdateAppDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.update_available))
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.new_version_of_the_app_))
        builder.setPositiveButton(getString(R.string.update)) { _: DialogInterface?, _: Int ->
            goToApp()
            finishAffinity()
        }
        val dialog = builder.create()
        dialog.show()
        try {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.find_printer_color))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun goToApp() {
        fireLogEvent("update", "user_move_to_update", "Call")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun proceedFurther() {
        if (SharedPreferenceHelper.getBoolean(this, Constent.IS_CONSENT_KEY)) {
            initializeMobileAdsSdk()
        } else {
            initAdsConsent()
        }
    }

    private fun initializeMobileAdsSdk() {
        initInterstitial()

    }

    @SuppressLint("SetTextI18n")
    private fun initAdsConsent() {
        binding.progressTv.text = getString(R.string.consent_fetching)
        animateProgressBar(binding.progress.progress, 66, 2000)

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        googleMobileAdsConsentManager!!.gatherConsent(this) { consentError: FormError? ->
            if (googleMobileAdsConsentManager!!.canRequestAds)
                fireLogEvent(
                    "splash_consent_accept",
                    "Splash Activity",
                    "Call"
                ) else fireLogEvent("splash_consent_reject", "Splash_Activity", "Call")

            SharedPreferenceHelper.setBoolean(this, Constent.IS_CONSENT_KEY, true)
            initializeMobileAdsSdk()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initInterstitial() {
       // (application as MyApplicationClass).loadAd(this)

        binding.progressTv.text = getString(R.string.loading_ad)
        val interstitialAdsId = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            SharedPreferenceHelper.getString(this, IdesConstent.SPLASH_INTERSTITIAL_AD_ID)
        }
        if (interstitialAdsId != null) {
            progressAnimator?.cancel()
            showAdLoadingView = true
            animateProgressBar(binding.progress.progress, 100, 3000)
            fireLogEvent("splash_ad_load_call", "Splash_Activity", "Call")
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                this,
                interstitialAdsId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        fireLogEvent(
                            "splash_ad_failed",
                            adError.code.toString() + " : " + adError.message,
                            "Call"
                        )
                        mInterstitialAd = null
                        showAdLoadingView = false
                        goNextOnAnimEnd = true
                        animateProgressBar(binding.progress.progress, 100, 10)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        fireLogEvent("splash_ad_load", "Splash_Activity", "Call")
                        showAdLoadingView = false
                        animateProgressBar(binding.progress.progress, 100, 10)
                        binding.adLoadingLayout.layAdLoading.visibility = View.VISIBLE
                        mInterstitialAd = interstitialAd
                        if (isAppInBackground) {
                            showInterstitialAds()
                        }
                    }
                })
        } else {
            showAdLoadingView = false
            goNextOnAnimEnd = true
            animateProgressBar(binding.progress.progress, 100, 10)
        }
    }

    fun showInterstitialAds() {
        mInterstitialAd!!.show(this)
        mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                myCTR++
                when (myCTR) {
                    10 -> {
                        Log.d("TAG", "ctr_splash_inter_: $myCTR")
                        fireLogEvent(
                            "ctr_splash_inter_10",
                            "PremiumActivity",
                            "PremiumActivity"
                        )
                    }

                    7 -> {
                        Log.d("TAG", "ctr_splash_inter_: $myCTR")
                        fireLogEvent(
                            "ctr_splash_inter_7",
                            "PremiumActivity",
                            "PremiumActivity"
                        )
                    }

                    4 -> {
                        Log.d("TAG", "ctr_splash_inter_: $myCTR")
                        fireLogEvent(
                            "ctr_splash_inter_4",
                            "PremiumActivity",
                            "PremiumActivity"
                        )
                    }

                    3 -> {
                        Log.d("TAG", "ctr_splash_inter_: $myCTR")
                        fireLogEvent(
                            "ctr_splash_inter_3",
                            "PremiumActivity",
                            "PremiumActivity"
                        )
                    }
                }
            }

            override fun onAdDismissedFullScreenContent() {
                fireLogEvent("splash_ad_dismiss", "Splash_Activity", "Call")

                mInterstitialAd = null
                binding.progress.visibility = View.INVISIBLE

                binding.adLoadingLayout.layAdLoading.visibility = View.GONE
                goToNext()

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                fireLogEventExtra(
                    "splash_ad_show_failed",
                    adError.code.toString() + " : " + adError.message,
                    "Call"
                )

                mInterstitialAd = null
                binding.adLoadingLayout.layAdLoading.visibility = View.GONE
                goToNext()
            }

            override fun onAdImpression() {
                fireLogEvent("splash_onAdImpression", "Splash_Activity", "Call")
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null
                fireLogEvent("splash_onAdShowed", "Splash_Activity", "Call")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isAppInBackground) {
            isAppInBackground = true
            if (mInterstitialAd != null) {
                Handler(Looper.myLooper()!!).postDelayed({
                    showInterstitialAds()

                }, 300)
            }
        }

    }

    private fun applyTheme() {
        val nightTheme = SharedPreferenceHelper.getBoolean(this, Constent.THEME_MOD)
        when (nightTheme) {
            true -> {

                setNightMode(true)
            }

            false -> {
                setNightMode(false)
            }
        }
        decideThemeSpecial()
    }

    private fun animateProgressBar(from: Int, to: Int, duration: Long) {
        progressAnimator?.cancel()
        progressAnimator =
            ObjectAnimator.ofInt(binding.progress, "progress", from, to).apply {
                this.duration = duration
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {

                        if (showAdLoadingView) {
                            binding.adLoadingLayout.layAdLoading.visibility = View.VISIBLE
                        }
                        if (goNextOnAnimEnd) {
                            goToNext()
                        }
                    }
                })
                start()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        Constent.isInterstitialAdShowing=false
    }
}