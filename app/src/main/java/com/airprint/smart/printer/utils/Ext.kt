package com.airprint.smart.printer.utils

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.print.PrintManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.airprint.smart.printer.R
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.MiniAdLoadingPopupBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun setNightMode(state: Boolean) {
    if (state) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

inline fun <reified T : Any> createIntent(mContext: Context) = Intent(mContext, T::class.java)

inline fun <reified T : Any> Context.launchActivity(noinline bundle: Intent.() -> Unit = {}) {
    val intent = createIntent<T>(this)
    intent.bundle()
    startActivity(intent)
}

fun Activity.connectToPrinter(
    ip: String,
    port: Int,
    printData: ByteArray,
    callBack: (isConnected: Boolean) -> Unit
) {

    saveConnectionValue(ip, port, false, callBack)
}

fun Activity.saveConnectionValue(
    ip: String,
    port: Int,
    isSaved: Boolean,
    callBack: (isConnected: Boolean) -> Unit
) {
    SharedPreferenceHelper.setBoolean(this, Constent.PRINTER_CONNECTION, true)
    SharedPreferenceHelper.setString(this, Constent.PRINTER_IP, ip)
    SharedPreferenceHelper.setInt(this, Constent.PRINTER_PORT, port)

    val resultIntent = Intent()
    if (!isSaved) {
        this.setResult(Activity.RESULT_OK, resultIntent)
    }
    callBack.invoke(isSaved)
}



fun Context.isInternetConnected(): Boolean {
    val connManager =
        getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connManager.activeNetwork ?: return false
    val networkCapabilities = connManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun Activity.customToast(success: Boolean, message: String, customDurationMillis: Long) {
    val layout = layoutInflater.inflate(
        R.layout.apply_theme_toast,
        null
    )
    val card = layout.findViewById<CardView>(R.id.button_card_parent)
    if (success) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lock_bg_green))
    } else {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.alert_color))
    }

    val textView = layout.findViewById<TextView>(R.id.toast_tv)
    textView.text = message

    val toast = Toast(this).apply {
        duration = Toast.LENGTH_SHORT
        setGravity(Gravity.BOTTOM, 0, 100)
        view = layout
        show()
    }

    Handler(Looper.getMainLooper()).postDelayed({
        toast.cancel()
    }, customDurationMillis)
}

fun Context.isCameraPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

fun View.getBitmapFromView(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}

fun Context.uriToBitmap(uri: Uri): Bitmap? {
    return try {
        val inputStream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun getCurrentDateTimeForImageName(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return dateFormat.format(Date())
}
 fun uriToFile(uri: Uri): File? {
    return uri.path?.let { path ->
        File(path)
    }
}


fun Date.formatToCustomDateTime(): String {
    val dateFormat = SimpleDateFormat("dd/MMM/yyyy : hh:mm a", Locale.getDefault())
    return dateFormat.format(this)
}

// Function to get the formatted current date and time
fun getCurrentFormattedDateTime(): String {
    return Date().formatToCustomDateTime()
}

fun Context.isNetworkAvailable(): Boolean {
    val connManager =
        getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connManager.activeNetwork ?: return false
    val networkCapabilities = connManager.getNetworkCapabilities(network) ?: return false

    Log.d("speed", "checkNetworkSpeed: ${networkCapabilities.linkDownstreamBandwidthKbps / 1000}")

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

}

data class LanguageModel(
    var flag: Int? = null,
    var languageName: String? = null,
    var languageCode: String? = null,
    var isSelected: Boolean = false
)

fun List<LanguageModel>.getPredefinedLanguages(): List<LanguageModel> {
    return listOf(
        LanguageModel(flag = R.drawable.flag_english, languageName = "English", languageCode = "en", isSelected = true),
        LanguageModel(flag = R.drawable.flag_spnish, languageName = "Spanish", languageCode = "es", isSelected = false),
        LanguageModel(flag = R.drawable.flag_portuguese, languageName = "Portuguese", languageCode = "pt", isSelected = false),
        LanguageModel(flag = R.drawable.flag_french, languageName = "French", languageCode = "fr", isSelected = false),
        LanguageModel(flag = R.drawable.flag_vietnam, languageName = "Vietnamese", languageCode = "vi", isSelected = false),
        LanguageModel(flag = R.drawable.flag_africa, languageName = "Brazilian", languageCode = "pt", isSelected = false),
        LanguageModel(flag = R.drawable.flag_afriken, languageName = "South African", languageCode = "xh", isSelected = false),
        LanguageModel(flag = R.drawable.flag_chinese, languageName = "Chinese", languageCode = "zh", isSelected = false)
    )
}


fun Activity.decideTheme(){
    val  isDark= SharedPreferenceHelper.getBoolean(this, Constent.THEME_MOD,false)
    if (isDark){
        isAppStatusBarLightForWhiteColor(false)
    }else{
        isAppStatusBarLightForWhiteColor(true)
    }
}
fun Activity.decideThemeSpecial(){
    val  isDark= SharedPreferenceHelper.getBoolean(this, Constent.THEME_MOD,false)
    if (isDark){
        isAppStatusBarLightForWhiteColorForSplash(false)
    }else{
        isAppStatusBarLightForWhiteColorForSplash(true)
    }
}

fun Activity.isAppStatusBarLightForWhiteColor(b: Boolean) {
    window.statusBarColor = ContextCompat.getColor(this, R.color.app_color)
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = b
    }
}


fun Activity.isAppStatusBarLightForWhiteColorForSplash(b: Boolean) {
    window.statusBarColor = ContextCompat.getColor(this, R.color.above_color)
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = b
    }
}

fun Context.rateUsApp() {
    val uri = Uri.parse("market://details?id=$packageName")
    val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
        startActivity(myAppLinkToMarket)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.UNABLE_TO_FIND_MARKET_APP, Toast.LENGTH_LONG).show()
    }
}


fun Context.shareOurApp() {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.putExtra(Intent.EXTRA_SUBJECT, "Download Gps Trip Route Finder")
    intent.putExtra(
        Intent.EXTRA_TEXT,
        getString(R.string.share_app_message) + Uri.parse("https://play.google.com/store/apps/details?id=com.airprint.smart.printer")
    )
    intent.type = "text/plain"
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    startActivity(intent)

}

fun Context.moreApp() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/developer?id=App+Arena+Studio")
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/developer?id=App+Arena+Studio")
            )
        )
    }
}

fun Context.dialogPrivacy() {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://sites.google.com/view/air-printer-privacy-policy/home")
    )
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show()
    }
}
private lateinit var animatorSetEnableBtn: AnimatorSet
private lateinit var scaleX: ObjectAnimator
private lateinit var scaleY: ObjectAnimator

fun Activity.setUpAnimation(view: View){
    animatorSetEnableBtn= AnimatorSet()
    scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.1f, 0.9f)
    scaleY = ObjectAnimator.ofFloat(view,"scaleY", 0.9f, 1.1f, 0.9f)
    scaleX.repeatCount = ObjectAnimator.INFINITE
    scaleX.duration = 2000
    scaleY.repeatCount = ObjectAnimator.INFINITE
    scaleY.duration = 2000

    animatorSetEnableBtn.playTogether(scaleX, scaleY)
    animatorSetEnableBtn.start()
}


 fun Context.isPurchase():Boolean {
    return SharedPreferenceHelper.getBoolean(this,Constent.IS_PURCHASE_SUBS)||SharedPreferenceHelper.getBoolean(this,Constent.IS_PURCHASE_IN_AP)
}

fun Activity.makeDialog(): Dialog {
    val exitBinding = MiniAdLoadingPopupBinding.inflate(LayoutInflater.from(this))
    return Dialog(this).apply {
        setContentView(exitBinding.root)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams = attributes
            layoutParams?.gravity = Gravity.CENTER
            attributes = layoutParams
        }
        setCancelable(false)
    }
}
  fun Context.printPdf(pdfFile: File) {
    val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "${getString(R.string.app_name)} Document"
    val printAdapter = com.airprint.smart.printer.adapter.PdfDocumentAdapter(pdfFile)
    printManager.print(jobName, printAdapter, null)
}
