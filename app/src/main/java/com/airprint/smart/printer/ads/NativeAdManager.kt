package com.airprint.smart.printer.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.airprint.smart.printer.R
import com.airprint.smart.printer.utils.Constent
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object NativeAdManager {
    private const val TAG = "NativeAdManager"
    var preLoadNativeAd: NativeAd?=null
    fun requestAdmobNativeAd(
        context: Activity,
        adUnit: String,
        frameLayout: FrameLayout,
        loadingCard: LinearLayout?,
        fromCalled:String
    ) {
        var mNativeAd: NativeAd? = null

        val builder = AdLoader.Builder(context, adUnit)
            .forNativeAd { nativeAd: NativeAd? ->
                if (mNativeAd != null) {
                    mNativeAd!!.destroy()
                }
                loadingCard!!.visibility = View.GONE

                mNativeAd = nativeAd

                val adView: NativeAdView

                when (fromCalled) {
                    Constent.LARGE_NATIVE_AD -> {
                        adView =
                            context.layoutInflater.inflate(
                                R.layout.gnt_medium_template_view,
                                null
                            ) as NativeAdView
                        populateUnifiedNativeAdViewSmall(mNativeAd, adView)

                    }
                    Constent.SMALL_WITH_MEDIA -> {
                        adView =
                            context.layoutInflater.inflate(
                                R.layout.new_small_native_layout,
                                null
                            ) as NativeAdView
                        populateUnifiedNativeAdView(mNativeAd, adView)
                    }
                    else -> {
                        adView =
                            context.layoutInflater.inflate(
                                R.layout.exit_dialog_native_ad_templet,
                                null
                            ) as NativeAdView
                        populateUnifiedNativeAdViewSmallWithoutMedia(mNativeAd, adView)
                    }
                }

                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }

        val adLoader = builder.withAdListener(object : AdListener() {
            @SuppressLint("LogNotTimber")
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Failed to load native main ad: $loadAdError")
                loadingCard!!.visibility = View.GONE
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG, " native main ad: onAdLoaded")
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd?, adView: NativeAdView) {
        adView.iconView = adView.findViewById(R.id.app_icon)
        adView.headlineView = adView.findViewById(R.id.primary)
        adView.bodyView = adView.findViewById(R.id.body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.mediaView=adView.findViewById(R.id.media_view)
        (adView.headlineView as TextView?)!!.text = nativeAd!!.headline
        adView.mediaView!!.mediaContent = nativeAd.mediaContent

        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as Button?)!!.text = nativeAd.callToAction
        }

        adView.setNativeAd(nativeAd)
    }
    private fun populateUnifiedNativeAdViewSmall(nativeAd: NativeAd?, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as MediaView
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.starRatingView =adView.findViewById(R.id.ad_stars)
        (adView.headlineView as TextView?)!!.text = nativeAd!!.headline
        adView.mediaView!!.mediaContent = nativeAd.mediaContent

        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as AppCompatButton?)!!.text = nativeAd.callToAction
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView!!.visibility = View.GONE
        } else {
            (adView.advertiserView as TextView?)!!.text = nativeAd.advertiser
            adView.advertiserView!!.visibility = View.GONE
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView!!.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar?)!!.rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView!!.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    private fun populateUnifiedNativeAdViewSmallWithoutMedia(
        nativeAd: NativeAd?,
        adView: NativeAdView
    ) {
        adView.iconView = adView.findViewById(R.id.app_icon)
        adView.headlineView = adView.findViewById(R.id.primary)
        adView.bodyView = adView.findViewById(R.id.body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

        (adView.headlineView as TextView?)!!.text = nativeAd!!.headline
        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.INVISIBLE
        } else {
            adView.iconView!!.visibility = View.VISIBLE
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
        }

        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as AppCompatButton?)!!.text = nativeAd.callToAction
        }
        /*  if (nativeAd.starRating == null) {
              adView.starRatingView!!.visibility = View.GONE
          } else {
              (adView.starRatingView as RatingBar?)!!.rating = nativeAd.starRating!!.toFloat()
              adView.starRatingView!!.visibility = View.VISIBLE
          }*/


        adView.setNativeAd(nativeAd)
    }



    fun requestPreLoadNativeAd(
        context: Activity,
        adUnit: String,
        onAdLoaded: (NativeAd?) -> Unit,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        var mNativeAd: NativeAd? = null

        val builder = AdLoader.Builder(context, adUnit)
            .forNativeAd { nativeAd: NativeAd? ->
                if (mNativeAd != null) {
                    mNativeAd!!.destroy()
                }
                mNativeAd = nativeAd
                onAdLoaded(mNativeAd)  // Return the native ad when loaded
            }

        val adLoader = builder.withAdListener(object : AdListener() {
            @SuppressLint("LogNotTimber")
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Failed to load native ad: $loadAdError")
                onAdFailed(loadAdError)  // Return error callback when failed
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e(TAG, "Native ad: onAdLoaded")
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }






}