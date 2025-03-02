package com.airprint.smart.printer.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityPremiumBinding
import com.airprint.smart.printer.premium.InAppHelper
import com.airprint.smart.printer.premium.OnUpdateInAppHelperListener
import com.airprint.smart.printer.premium.OnUpdateSubscriptionListener
import com.airprint.smart.printer.premium.SubscriptionHelper
import com.airprint.smart.printer.premium.SubscriptionHelper.Companion.subscriptionId
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.decideThemeSpecial
import com.airprint.smart.printer.utils.dialogPrivacy
import com.airprint.smart.printer.utils.isAppStatusBarLightForWhiteColorForSplash
import com.airprint.smart.printer.utils.isPurchase
import com.airprint.smart.printer.utils.setNightMode
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.google.common.collect.ImmutableList

class PremiumActivity : BaseActivity<ActivityPremiumBinding>() {
    private var subscriptionHelper: SubscriptionHelper? = null
    private var inAppHelper: InAppHelper? = null
    private var isLifeTimePurchase=false
    private val onUpdateSubscriptionListener: OnUpdateSubscriptionListener = object : OnUpdateSubscriptionListener {
        override fun setSubscriptionPrice(string: String) {
            setSubsSkuValues(string)
        }

        override fun updateSubscriptionUI() {

        }
    }

    private val onUpdateInAppHelperListener: OnUpdateInAppHelperListener = object : OnUpdateInAppHelperListener {
        override fun setInAppPrice(string: String) {
            setInAppSkuValues(string)
        }

        override fun updateInAppUI() {

        }

    }



    override fun setupViewBinding(): ActivityPremiumBinding {
        return ActivityPremiumBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.post {
            decideThemeSpecial()
            checkSubscription()
            initView()
        }
    }

    private fun checkSubscription() {
        subscriptionHelper = SubscriptionHelper.getInstance(this)
        subscriptionHelper?.setListener(onUpdateSubscriptionListener)
        subscriptionHelper?.initializeBillingClient(this)
        SubscriptionHelper.billingClient = subscriptionHelper?.connectToBillingService()


        inAppHelper = InAppHelper.getInstance(this)
        inAppHelper?.setListener(onUpdateInAppHelperListener)
        inAppHelper?.initializeBillingClient(this)
        InAppHelper.billingClient = inAppHelper?.connectToBillingService()
    }

    private fun initView() {
        listener()

    }

    private fun listener() {
        binding.apply {
            fun updateSelection(selectedButton: View, showOffIcon: Boolean) {
                weeklyBtn.strokeWidth = if (selectedButton == weeklyBtn) 2 else 0
                monthlyBtn.strokeWidth = if (selectedButton == monthlyBtn) 2 else 0
                lifeTimeBtn.strokeWidth = if (selectedButton == lifeTimeBtn) 2 else 0

            }

            weeklyBtn.setSafeOnClickListener {
                subscriptionId=Constent.WEEKLY_ID
                isLifeTimePurchase=false
                updateSelection(weeklyBtn, showOffIcon = false)
            }
            monthlyBtn.setSafeOnClickListener {
                subscriptionId=Constent.WEEKLY_ID
                isLifeTimePurchase=false
                updateSelection(monthlyBtn, showOffIcon = true)
            }
            lifeTimeBtn.setSafeOnClickListener {
                isLifeTimePurchase=true
                updateSelection(lifeTimeBtn, showOffIcon = false)
            }
            continueBtn.setSafeOnClickListener {
                if (isPurchase()){
                    Toast.makeText(this@PremiumActivity, "Already purchase", Toast.LENGTH_SHORT).show()
                }else{
                    if (isLifeTimePurchase){
                        launchInAppBillingFlow(inAppHelper?.lifeTimeSkuDetails)
                    }else{
                        launchSubscriptionBillingFlow(subscriptionHelper?.subscriptionSkuDetails)
                    }

                }
            }
            termOfUseBottomTv.setSafeOnClickListener {
                dialogPrivacy()
            }
            ppBottomTv.setSafeOnClickListener {
                dialogPrivacy()
            }
            closeBtn.setSafeOnClickListener {
                finish()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setSubsSkuValues(string: String) {
        try {
            if (subscriptionHelper?.subscriptionSkuDetails == null) {
                binding.weeklyPrice.text= "${SharedPreferenceHelper.getString(this@PremiumActivity,Constent.WEEKLY_PRICE)} / weekly"

            } else {
                binding.monthlyPrice.text= "${SharedPreferenceHelper.getString(this@PremiumActivity,Constent.MONTHLY_PRICE)} / monthly"

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setInAppSkuValues(string: String) {
        try {
            if (inAppHelper?.lifeTimeSkuDetails == null) {
                binding.lifeTimePrice.text= "${SharedPreferenceHelper.getString(this@PremiumActivity,Constent.LIFE_TIME_PRICE)} / Year"

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun launchInAppBillingFlow(skuDetails: ProductDetails?) {
        if (!isPurchase()) {
            if (skuDetails != null) {
                startInAppPurchase(skuDetails)
            } else {
                inAppHelper?.startConnection(true)
            }
        } else {
            Toast.makeText(this, "Already owns this product", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "launchBillingFlow: User already owns this product")
        }
    }


    private fun launchSubscriptionBillingFlow(skuDetails: ProductDetails?) {
        if (!isPurchase()) {
            if (skuDetails != null) {
                startSubscriptionPurchase(skuDetails)
            } else {
                subscriptionHelper?.startConnection(true)
            }
        } else {
            Toast.makeText(this, "Already owns this product", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "launchBillingFlow: User already owns this product")
        }
    }

    private fun startInAppPurchase(skuDetails: ProductDetails?) {
        if (skuDetails != null) {
            val flowParams: BillingFlowParams =
                BillingFlowParams.newBuilder().setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(skuDetails).build()
                    )
                ).build()
            InAppHelper.billingClient?.launchBillingFlow(this, flowParams)
        } else {
            inAppHelper?.startConnection(true)
        }
    }

    private fun startSubscriptionPurchase(skuDetails: ProductDetails?) {
        if (skuDetails != null) {
            Log.d(TAG, "startPurchase: ${skuDetails}")
            val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(skuDetails)
            if (skuDetails.subscriptionOfferDetails?.isNotEmpty() == true) {
                val offerToken = skuDetails.subscriptionOfferDetails!!.first().offerToken
                productDetailsParamsBuilder.setOfferToken(offerToken)
            }

            val flowParams: BillingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    ImmutableList.of(
                        productDetailsParamsBuilder.build()
                    )
                ).build()

            SubscriptionHelper.billingClient?.launchBillingFlow(this, flowParams)
        } else {
            Log.d(TAG, "sku list is null---------->: ${skuDetails}")
            subscriptionHelper?.startConnection(true)
        }
    }

}