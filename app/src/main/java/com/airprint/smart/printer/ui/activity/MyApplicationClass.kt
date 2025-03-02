package com.airprint.smart.printer.ui.activity

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.Constent.isInterstitialAdShowing
import com.airprint.smart.printer.utils.setNightMode
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date


class MyApplicationClass : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    private val TAG = "AppClass"
    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    companion object {
        lateinit var instance: MyApplicationClass
            private set
        lateinit var instance1: Context
            private set
    }

    override fun onCreate() {
        super<Application>.onCreate()
        instance = this
        instance1 = this
        uiThemeCallBack()

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
    }

    private fun uiThemeCallBack() {
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    Log.d(TAG, "Night mode is active")
                    setNightMode(true)
                    SharedPreferenceHelper.getBoolean(
                        this@MyApplicationClass,
                        Constent.THEME_MOD,
                        true
                    )
                } else if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
                    Log.d(TAG, "Light mode is active")
                    setNightMode(false)
                    SharedPreferenceHelper.getBoolean(
                        this@MyApplicationClass,
                        Constent.THEME_MOD,
                        false
                    )
                }
            }

            override fun onLowMemory() {}
            override fun onTrimMemory(level: Int) {}
        })
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    fun showAdIfAvailable(activity: Activity) {
        appOpenAdManager.showAdIfAvailable(activity)
    }

    fun loadAd(activity: Activity) {
        appOpenAdManager.loadAd(activity)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false
        private var loadTime: Long = 0

        fun loadAd(activity: Activity) {
            if (isLoadingAd || isAdAvailable()) return

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                activity,
                Constent.AD_UNIT_ID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Log.d(TAG, "onAdLoaded.")
                        Toast.makeText(activity, "Ad Loaded", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        Log.d(TAG, "onAdFailedToLoad: ${loadAdError.message}")
                    }
                }
            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val timeDiff = Date().time - loadTime
            return timeDiff < numHours * 3600000
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(
            activity: Activity) {
            if (isShowingAd) return
            if (!isAdAvailable()) {
                Log.d(TAG, "Ad is not ready.")
                loadAd(activity)
                return
            }
            if (isInterstitialAdShowing){
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(TAG, "Ad Dismissed.")
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    Log.d(TAG, "Ad Failed to Show: ${adError.message}")
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }

            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }


}
