package com.airprint.smart.printer.ads

import android.app.Activity
import android.app.Dialog
import android.util.Log
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.ui.activity.MainActivity.Companion.canAdLoad
import com.airprint.smart.printer.utils.Constent.isInterstitialAdShowing
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdsManager {
    private var interstitial: InterstitialAd? = null
    private var TAG="Ad_query"
    fun  loadInitInterstitial(context: Activity, adOriginalId:String, isNetAvail:Boolean, callBack: AdsCallBack, adControl:Boolean,dialog: Dialog?,action:String) {
        val adId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else adOriginalId
        if (adId.isNotEmpty()&&isNetAvail&&adControl&&canAdLoad) {
            dialog?.show()
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitial = null
                        canAdLoad=false
                        dialog?.dismiss()
                        callBack.onAdLoadToFail(action)
                        Log.d(TAG, "onAdFailedToLoad: ")
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "onAdLoaded: ")
                        dialog?.dismiss()
                        interstitial = interstitialAd
                        showInterstitialAds(context,callBack,action)

                    }
                })
        } else {
            Log.d(TAG, "loadInitInterstitial: ${canAdLoad}")
            canAdLoad=true
            callBack.onAdLoadToFail(action)
        }

    }

     private fun  showInterstitialAds(context: Activity, callBack: AdsCallBack,action: String) {
        interstitial!!.show(context)
        interstitial!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
               callBack.onAdClicked(action)
            }

            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                canAdLoad=false
                isInterstitialAdShowing=false
                callBack.onAdDismiss(action)

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitial = null
                canAdLoad=false
                isInterstitialAdShowing=false
                callBack.onAdShowToFail(action)
            }

            override fun onAdImpression() {
                callBack.onAdImpression(action)

            }

            override fun onAdShowedFullScreenContent() {
                interstitial = null
                isInterstitialAdShowing=true
                callBack.onAdShow(action)

            }
        }
    }


}
