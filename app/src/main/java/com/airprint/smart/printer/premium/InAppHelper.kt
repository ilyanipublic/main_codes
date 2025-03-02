package com.airprint.smart.printer.premium
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.utils.Constent
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
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
import kotlin.math.log

class InAppHelper private constructor(activity: AppCompatActivity) : BillingClientStateListener,
    PurchasesUpdatedListener {

    private val TAG = "InAppHelper"

    companion object {
        private var instance: InAppHelper? = null
        var billingClient: BillingClient? = null

        @Synchronized
        fun getInstance(activity: AppCompatActivity): InAppHelper {
            if (instance == null || instance?.activityContext == null) {
                instance = InAppHelper(activity)
            }
            return instance!!
        }
    }

    private val activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)
    private val activityContext: Context?
        get() = activityRef.get()

    private var onUpdatePremiumListener: OnUpdateInAppHelperListener? = null
    private var reconnectMilliseconds: Long = 1000
    private var recallStatus = false

    var selectedProductId: String = Constent.LIFETIME_ID
    var selectedProductType: String = BillingClient.ProductType.INAPP

    var lifeTimeSkuDetails: ProductDetails? = null
    private var currentPurchase: Purchase? = null

    fun setListener(listener: OnUpdateInAppHelperListener?) {
        onUpdatePremiumListener = listener
    }

    fun initializeBillingClient(context: Context) {
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        }
    }

    fun startConnection(status: Boolean) {
        recallStatus = status
        billingClient?.startConnection(this)
    }

    fun connectToBillingService(): BillingClient? {
        billingClient?.takeIf { !it.isReady }?.startConnection(this)
            ?: verifyPurchase()
        return billingClient
    }

    private fun verifyPurchase() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(selectedProductType).build(),
            purchasesResponseListener
        )
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            queryProductDetails()
        }
    }

    private val purchasesResponseListener = PurchasesResponseListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(purchases)
        } else {
            Log.e(TAG, "Error querying purchases: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchases(purchases: List<Purchase>?) {
        purchases?.forEach { purchase ->
            Log.d(TAG, purchase.purchaseToken)
            currentPurchase = purchase
            activityContext?.let { SharedPreferenceHelper.setBoolean(it, Constent.IS_PURCHASE_IN_AP, true) }
        } ?: run {
            activityContext?.let { SharedPreferenceHelper.setBoolean(it, Constent.IS_PURCHASE_IN_AP, false) }
        }
    }

    fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(selectedProductId)
            .setProductType(selectedProductType)
            .build()

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient?.queryProductDetailsAsync(queryParams, productDetailsResponseListener)
    }

    override fun onBillingServiceDisconnected() {
        retryBillingServiceConnection()
    }

    private fun retryBillingServiceConnection() {
        Handler(Looper.getMainLooper()).postDelayed({
            billingClient?.startConnection(this)
        }, reconnectMilliseconds)
        reconnectMilliseconds *= 2
    }

    private val productDetailsResponseListener =
        ProductDetailsResponseListener { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "code is okay------>${billingResult.responseCode}: ")
                productDetailsList.forEach { productDetails ->
                    if (productDetails.productId == selectedProductId) {
                        lifeTimeSkuDetails = productDetails
                        val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                        price?.let { formattedPrice ->
                            activityContext?.let { context ->
                                SharedPreferenceHelper.setString(context, Constent.LIFE_TIME_PRICE, formattedPrice)
                            }
                            onUpdatePremiumListener?.setInAppPrice(formattedPrice)
                        }
                    }
                }
            } else {
                Log.d(TAG, "Failed to fetch product details: ${billingResult.responseCode}")
            }
        }


    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (verifyValidSignature(purchase.originalJson, purchase.signature)) {
                    setPremium(purchase)
                }
            }
        } else {
            errorHandle(billingResult.responseCode)
        }
    }

    private fun setPremium(purchase: Purchase) {
        if (purchase.products.contains(selectedProductId)) {
            activityContext?.let { SharedPreferenceHelper.setBoolean(it, Constent.IS_PURCHASE_IN_AP, true) }
        }
        acknowledgePurchase(purchase)
        onUpdatePremiumListener?.updateInAppUI()
    }

    private fun verifyValidSignature(signedData: String?, signature: String?): Boolean {
        return try {
            Security.verifyPurchase(signedData.orEmpty(), signature.orEmpty())
        } catch (e: IOException) {
            false
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(acknowledgeParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                }
            }
        }
    }

    private fun errorHandle(responseCode: Int) {
        val errorMsg = when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Check Your Network Connection"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own this item."
            else -> "An error occurred."
        }
        activityContext?.let {
            Toast.makeText(it, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
