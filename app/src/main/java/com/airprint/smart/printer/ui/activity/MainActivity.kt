package com.airprint.smart.printer.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.AdsCallBack
import com.airprint.smart.printer.ads.InterstitialAdsManager
import com.airprint.smart.printer.ads.NativeAdManager
import com.airprint.smart.printer.ads.RewardedAdManager
import com.airprint.smart.printer.databinding.ActivityMainBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.RatingDialogBinding
import com.airprint.smart.printer.utils.ControlConsent
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.isCameraPermissionGranted
import com.airprint.smart.printer.utils.isInternetConnected
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.makeDialog
import com.airprint.smart.printer.utils.openAppSettings
import com.airprint.smart.printer.utils.rateUsApp
import com.airprint.smart.printer.utils.setNightMode
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.google.android.material.textview.MaterialTextView

class MainActivity :BaseActivity<ActivityMainBinding>() ,AdsCallBack{
    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            val uriList = ArrayList(uris)
            launchActivity<PdfViewActivity2> {
                putParcelableArrayListExtra(Constent.IMAGE_URI, uriList)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    private var isRate=false
    private  var exitDialog: Dialog? = null
  private  val selectPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { pdfUri ->
                getPrint(pdfUri)
            }
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
               launchActivity<ScanActivity>()
            } else {
                openAppSettings()

            }

        }


    companion object{
        var canAdLoad=false
    }
    private val dialog: Dialog by lazy { makeDialog() }

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        nativeAds()
        binding.root.post {
            decideTheme()
            inItViews()
            backPressedCalled()
        }
    }
    private fun inItViews() {
        listener()
    }

    private fun listener() {
        binding.apply {
            connectionCard.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.CONNECTIVITY_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_CONNECTIVITY)


            }
            photoView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.PHOTO_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_PIC_PHOTO_OPEN)

            }
            binding.documentsView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.DOCUMENTS_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_DOCUMENTS_OPEN)


            }
            webpageView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.WEBPAGE_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_WEBPAGE_OPEN)


            }
            emailsView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.EMAIL_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_EMAIL_OPEN)


            }
            clipBoardView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.CLIP_BOARD_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_CLIP_BOARD_OPEN)


            }
            scanView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@MainActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@MainActivity, adId, isInternetConnected(),
                    this@MainActivity, SharedPreferenceHelper.getBoolean(this@MainActivity,
                        ControlConsent.SCANNER_TOOL_CLICK_INTERSTITIAL_CONTROL), dialog, Constent.ACTION_SCANNER_OPEN)

            }
            settingBtn.setSafeOnClickListener {
                launchActivity<SettingActivity>()
            }
            probtn.setSafeOnClickListener {
                launchActivity<PremiumActivity>()
            }

        }
    }
    private fun startWebpage(url: String, i: Int) {
          launchActivity<OpenWebPageActivity> {
              putExtra(Constent.TYPE_OF_URL,url)
              putExtra(Constent.SELECT_POS,i)
          }

    }
    private fun applyTheme() {
        val nightTheme = SharedPreferenceHelper.getBoolean(this@MainActivity,Constent.THEME_MOD,)
        when (nightTheme) {
            true -> {
                setNightMode(true)
            }

            false -> {
                setNightMode(false)
            }
        }
    }
    private fun getPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        selectPdfLauncher.launch(Intent.createChooser(intent, "Select PDF"))
    }

    private fun nativeAds() {
        if (/*SharedPreferenceHelper.getBoolean(this, ControlConsent.HOME_NATIVE_CONTROL) &&*/ isNetworkAvailable()) {
            val adUnit = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                SharedPreferenceHelper.getString(this, IdesConstent.HOME_NATIVE_ID)
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
    private fun showRewardedAdDialog() {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.rewarded_ad_popup_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.CENTER
        layoutParams?.width = (binding.main.width - resources.getDimension(com.intuit.sdp.R.dimen._16sdp)).toInt()
        window?.attributes = layoutParams
        dialog.setCancelable(true)
        dialog.show()
        window?.setWindowAnimations(R.style.DialogAnimationInOut)
        val buttonWatchVideo = dialog.findViewById<ConstraintLayout>(R.id.buttonWatchVideo)
        val goWithPro = dialog.findViewById<ConstraintLayout>(R.id.goWithPro)
        val crossBtn = dialog.findViewById<AppCompatImageView>(R.id.crossBtn)
        buttonWatchVideo.setSafeOnClickListener {
            dialog.dismiss()
            watchVideo()
        }
        goWithPro.setSafeOnClickListener {
            dialog.dismiss()
            launchActivity<PremiumActivity>()
        }
        crossBtn.setSafeOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun watchVideo() {
        if (isInternetConnected()){
            binding.loading.layAdLoading.visibility = View.VISIBLE
            val adId = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/5224354917"
            } else {
                SharedPreferenceHelper.getString(this@MainActivity,IdesConstent.HOME_REWARDED_AD_ID)

            }

            RewardedAdManager.loadAndShowRewardedAd(
                this,
                adId!!,
                object : RewardedAdManager.AdLoadCallback {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Rewarded Interstitial Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(errorMessage: String) {
                        Log.e(TAG, "Failed to load Rewarded Interstitial Ad: $errorMessage")
                        binding.loading.layAdLoading.visibility = View.GONE
                        launchActivity<ConnectivityActivity>()
                    }
                },
                object : RewardedAdManager.AdShowCallback {
                    override fun onAdDismissed() {
                        fireLogEvent("click", "user_move_to_preview_screen_with_add", "lunch")
                        Log.d(TAG, "Rewarded Interstitial Ad dismissed.")
                        binding.loading.layAdLoading.visibility = View.GONE
                        launchActivity<ConnectivityActivity>()

                    }

                    override fun onAdFailedToShow(errorMessage: String) {
                        binding.loading.layAdLoading.visibility = View.GONE
                        Log.e(TAG, "Failed to show Rewarded Interstitial Ad: $errorMessage")
                    }

                    override fun onAdShowed() {
                        binding.loading.layAdLoading.visibility = View.GONE

                        Log.d(TAG, "Rewarded Interstitial Ad is showing.")
                    }

                    override fun onUserEarnedReward(rewardAmount: Int, rewardType: String) {
                        Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
                        binding.loading.layAdLoading.visibility = View.GONE

                        Log.d("AdShowCallback", "User earned reward: $rewardAmount $rewardType")
                    }
                }
            )

        }else{
            customToast(false, getString(R.string.internet_toast),2000)
        }
    }

    override fun onAdShow(string: String) {

    }

    override fun onAdClicked(string: String) {

    }

    override fun onAdDismiss(string: String) {
         decideWhichActionWillBeDone(string)
    }



    override fun onAdLoadToFail(string: String) {
        decideWhichActionWillBeDone(string)
    }

    override fun onAdShowToFail(string: String) {
        decideWhichActionWillBeDone(string)
    }

    override fun onAdImpression(string: String) {

    }
    private fun decideWhichActionWillBeDone(action: String) {
        when (action) {
            Constent.ACTION_CONNECTIVITY -> {
                launchActivity<ConnectivityActivity>()
            }
            Constent.ACTION_PIC_PHOTO_OPEN -> {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            Constent.ACTION_CLIP_BOARD_OPEN -> {
                launchActivity<ClipBoardActivity>()
            }
            Constent.ACTION_DOCUMENTS_OPEN -> {
                getPdf()
            }
            Constent.ACTION_WEBPAGE_OPEN -> {
                startWebpage(Constent.GOOGLE_URL, Constent.GOOGLE_URL_TAG)
            }
            Constent.ACTION_EMAIL_OPEN -> {
                startWebpage(Constent.EMAIL_URL, Constent.EMAIL_URL_TAG)
            }
            Constent.ACTION_SCANNER_OPEN -> {
                if (!isCameraPermissionGranted()) {
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                } else {
                    launchActivity<ScanActivity>()
                }
            }
        }
    }

    private fun backPressedCalled() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isRate||SharedPreferenceHelper.getBoolean(this@MainActivity,Constent.IS_RATING)){
                        showExitDialog()
                    }else{
                        isRate=true
                        showRatingDialog()
                    }
                }
            })
    }
    private fun showExitDialog() {
        exitDialog= Dialog(this@MainActivity)
        dialog.setContentView(R.layout.exit_dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.width = (binding.main.width - resources.getDimension(com.intuit.sdp.R.dimen._16sdp)).toInt()
        window?.attributes = layoutParams
        dialog.setCancelable(true)

        window?.setWindowAnimations(R.style.DialogAnimationInOut)
        val cancelBtn = dialog.findViewById<MaterialTextView>(R.id.cancelBtn)
        val confirmBtn = dialog.findViewById<MaterialTextView>(R.id.confirmBtn)
        cancelBtn.setSafeOnClickListener {
            dialog.dismiss()

        }
        confirmBtn.setSafeOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
    }
    private fun showRatingDialog() {
        var rate = 1.0f
        val dialogBinding = RatingDialogBinding.inflate(LayoutInflater.from(this))
        exitDialog = Dialog(this)
        exitDialog?.setContentView(dialogBinding.root)
        exitDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = exitDialog?.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.width = (binding.main.width - resources.getDimension(com.intuit.sdp.R.dimen._16sdp)).toInt()

        dialogBinding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                when (rating) {
                    1.0f -> {
                        rate = 1.0f

                    }

                    2.0f -> {
                        rate = 2.0f

                    }

                    3.0f -> {
                        rate = 3.0f

                    }

                    4.0f -> {
                        rate = 4.0f

                    }

                    5.0f -> {
                        rate = 5.0f

                    }

                }
            }
        }
        dialogBinding.ratingBtn.setOnClickListener {
            if (rate>=4.0f) {
                 rateUsApp()
                exitDialog?.dismiss()
            } else {
                customToast(true, getString(R.string.thanks_for_your_feedback),200)
                exitDialog?.dismiss()
            }
            SharedPreferenceHelper.setBoolean(this,Constent.IS_RATING,true)
        }
        dialogBinding.crossBtn.setOnClickListener {
            exitDialog?.dismiss()
        }

        window?.attributes = layoutParams
        exitDialog?.setCancelable(true)
        window?.setWindowAnimations(R.style.DialogAnimationInOut)
        exitDialog?.show()

    }
}