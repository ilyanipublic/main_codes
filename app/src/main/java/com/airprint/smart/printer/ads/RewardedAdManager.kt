package com.airprint.smart.printer.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private const val TAG = "RewardedAdManager"
    private var adLoadCallback: AdLoadCallback? = null
    private var adShowCallback: AdShowCallback? = null

    interface AdLoadCallback {
        fun onAdLoaded()
        fun onAdFailedToLoad(errorMessage: String)
    }

    interface AdShowCallback {
        fun onAdDismissed()
        fun onAdFailedToShow(errorMessage: String)
        fun onAdShowed()
        fun onUserEarnedReward(rewardAmount: Int, rewardType: String)
    }

    // Method to show the rewarded ad
    fun showRewardedAd(activity: Activity, callback: AdShowCallback) {
        adShowCallback = callback
        rewardedAd?.let { ad ->
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward: $rewardAmount $rewardType")
                adShowCallback?.onUserEarnedReward(rewardAmount, rewardType)
            })
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            adShowCallback?.onAdFailedToShow("The rewarded ad wasn't ready yet.")
        }
    }

    // Combined method to load and show the rewarded ad
    fun loadAndShowRewardedAd(activity: Activity, adUnitId: String, loadCallback: AdLoadCallback, showCallback: AdShowCallback) {
        adLoadCallback = loadCallback
        adShowCallback = showCallback



        val adRequest = AdRequest.Builder()
            .build()

        RewardedAd.load(activity, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                adLoadCallback?.onAdLoaded()
                setFullScreenContentCallback()

                // Show the ad immediately after loading
                showRewardedAd(activity, adShowCallback!!)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                rewardedAd = null
                adLoadCallback?.onAdFailedToLoad(adError.message)
            }
        })
    }

    // Set full-screen content callback for the ad
    private fun setFullScreenContentCallback() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                adShowCallback?.onAdDismissed()
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                adShowCallback?.onAdFailedToShow(adError.message ?: "Unknown error")
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                adShowCallback?.onAdShowed()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
            }
        }
    }
}