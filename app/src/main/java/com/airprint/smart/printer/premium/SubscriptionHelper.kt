package com.airprint.smart.printer.premium




import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.utils.Constent
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import java.io.IOException
import java.lang.ref.WeakReference



class SubscriptionHelper private constructor(activity: AppCompatActivity) : BillingClientStateListener,
    PurchasesUpdatedListener {
    private val TAG = "SubscriptionHelper"

    companion object {
        private var subscriptionHelper: SubscriptionHelper? = null
        var billingClient: BillingClient? = null
        var subscriptionId: String=Constent.WEEKLY_ID

        @Synchronized
        fun getInstance(activity: AppCompatActivity): SubscriptionHelper {
            if (subscriptionHelper == null || subscriptionHelper?.activityContext == null) {
                subscriptionHelper = SubscriptionHelper(activity)
            }
            return subscriptionHelper as SubscriptionHelper
        }
    }

    private val activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)
    private val activityContext: Context?
        get() = activityRef.get()

    private var onUpdatePremiumListener: OnUpdateSubscriptionListener? = null
    var subscriptionSkuDetails: ProductDetails? = null
    private var currentPurchase: Purchase? = null
    private var reconnectMilliseconds: Long = 1000
    private var recallStatus = false

    fun setListener(premiumListener: OnUpdateSubscriptionListener?) {
        onUpdatePremiumListener = premiumListener
    }

    fun getCurrentPurchase(): Purchase? {
        return currentPurchase
    }

    fun initializeBillingClient(context: Context) {
        Log.d(TAG, "initializeBillingClient: $subscriptionId")
        if (billingClient == null)
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
    }

    fun getBillingClient(): BillingClient? {
        return billingClient
    }

    fun startConnection(status: Boolean) {
        recallStatus = status
        billingClient?.startConnection(this)
    }

    fun connectToBillingService(): BillingClient? {
        if (billingClient?.isReady == false)
            billingClient?.startConnection(this)
        else {
            verifyPurchase()
        }
        return billingClient
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        Log.d("abcd", subscriptionId)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val subscriptionSkus = mutableListOf<QueryProductDetailsParams.Product>()
            val productSubscription = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subscriptionId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            subscriptionSkus.add(productSubscription)
            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subscriptionSkus)
                .build()

            billingClient?.queryProductDetailsAsync(
                queryProductDetailsParams,
                productDetailsResponseListener
            )

            verifyPurchase()
        } else {
            Log.d(TAG, "onBillingSetupFinished: code does not match")
        }
    }

    override fun onBillingServiceDisconnected() {
        retryBillingServiceConnection()
    }

    private fun retryBillingServiceConnection() {
        Handler(Looper.getMainLooper()).postDelayed(
            { connectToBillingService() },
            reconnectMilliseconds
        )
        reconnectMilliseconds *= 2
    }

    private fun verifyPurchase() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            subscriptionResponseListener
        )
    }

    private val subscriptionResponseListener =
        PurchasesResponseListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "response--------->${billingResult.responseCode}: ")
                purchases.let {
                    if (it.isNotEmpty()) {
                        Log.d("abcd2", subscriptionId)
                        for (purchase in it) {
                            if (purchase.products.contains(subscriptionId)) {
                                currentPurchase = purchase
                                SharedPreferenceHelper.setBoolean(
                                    activity,
                                    Constent.IS_PURCHASE_SUBS,
                                    true
                                )
                               
                                Log.d(TAG, "is weekly purchse true: ")
                            }else if(purchase.products.contains(Constent.MONTHLY_ID)){
                                currentPurchase = purchase
                                SharedPreferenceHelper.setBoolean(
                                    activity,
                                    Constent.IS_PURCHASE_SUBS,
                                    true
                                )
                                Log.d(TAG, "is monthly purchse true: ")
                            }
                        }
                    } else {
                        Log.d(TAG, "is purchse false: ")
                        SharedPreferenceHelper.setBoolean(activity, Constent.IS_PURCHASE_SUBS, false)
                    }
                }
                //   val isSubscribed = SharePrefs.getPro(activityContext!!)
                //  Log.d(TAG, "${isSubscribed}: ")
            }
        }

    private val productDetailsResponseListener =
        ProductDetailsResponseListener { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { productDetails ->
                    val pricingPhaseList = productDetails.subscriptionOfferDetails
                        ?.getOrNull(0)?.pricingPhases?.pricingPhaseList

                    val formattedPrice = pricingPhaseList?.getOrNull(0)?.formattedPrice
                    val productId = productDetails.productId
                    when (productId) {
                        Constent.MONTHLY_ID -> {
                            formattedPrice?.let { price ->
                                SharedPreferenceHelper.setString(
                                    activity,
                                    Constent.MONTHLY_PRICE,
                                    price
                                )
                                Log.d(TAG, "Monthly Price: $price")
                            }
                        }
                        Constent.WEEKLY_ID -> {
                            formattedPrice?.let { price ->
                                SharedPreferenceHelper.setString(
                                    activity,
                                    Constent.WEEKLY_PRICE,
                                    price
                                )
                                Log.d(TAG, "Weekly Price: $price")
                            }
                        }
                    }

                    if (subscriptionId == productId) {
                        subscriptionSkuDetails = productDetails

                        formattedPrice?.let {
                            onUpdatePremiumListener?.setSubscriptionPrice(it)
                        }

                        Log.d(
                            TAG, "onProductDetailsResponse: SKU - $productId, Price - $formattedPrice"
                        )
                        if (recallStatus) {
                            recallStatus = false
                            subscriptionSkuDetails?.let { skuDetails ->
                                val offerToken =
                                    skuDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken ?: ""
                                val billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(
                                        listOf(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(skuDetails)
                                                .setOfferToken(offerToken)
                                                .build()
                                        )
                                    )
                                    .build()
                                billingClient?.launchBillingFlow(
                                    activityContext as AppCompatActivity,
                                    billingFlowParams
                                )
                            }
                        }
                    }
                }
            }
        }


    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            purchases.forEach { purchase ->
                try {
                    if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                        Toast.makeText(
                            activityContext,
                            activityContext?.getString(R.string.security_issue_not_valid),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            activityContext,
                            activityContext?.getString(R.string.purchase_success_message),
                            Toast.LENGTH_LONG
                        ).show()
                        setPremium(purchase, activityContext!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            errorHandle(responseCode)
        }
    }

    private fun setPremium(purchase: Purchase, context: Context) {
        if (purchase.products.contains(subscriptionId)) {
            SharedPreferenceHelper.setBoolean(context, Constent.IS_PURCHASE_SUBS, true)
        }

        acknowledgePurchase(purchase)

        onUpdatePremiumListener?.updateSubscriptionUI()
    }

    private fun verifyValidSignature(signedData: String?, signature: String?): Boolean {
        return try {
            Security.verifyPurchase(signedData.orEmpty(), signature.orEmpty())
        } catch (e: IOException) {
            Log.e(TAG, "Got an exception trying to validate a purchase: $e")
            false
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient?.acknowledgePurchase(
                    acknowledgePurchaseParams
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")

                    }
                }
            }
        }
    }

    private fun errorHandle(responseCode: Int) {
        when (responseCode) {
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.d(
                TAG,
                "onPurchasesUpdated: User Canceled"
            )

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Toast.makeText(
                activityContext,
                "Item already purchased.",
                Toast.LENGTH_LONG
            ).show()

            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> Toast.makeText(
                activityContext,
                "Item not available.",
                Toast.LENGTH_LONG
            ).show()

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> Toast.makeText(
                activityContext,
                "Service disconnected.",
                Toast.LENGTH_LONG
            ).show()

            BillingClient.BillingResponseCode.ERROR -> Toast.makeText(
                activityContext,
                "Billing error.",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    // Method to restore purchases
    fun restorePurchases() {
        verifyPurchase()
    }

    // Method to open subscription management
    fun openSubscriptionManagement() {
        try {
            val uri =
                Uri.parse("https://play.google.com/store/account/subscriptions?sku=$subscriptionId&package=${activityContext?.packageName}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            activityContext?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                activityContext,
                "Failed to open subscription management.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


