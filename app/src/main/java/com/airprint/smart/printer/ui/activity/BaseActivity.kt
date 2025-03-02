package com.airprint.smart.printer.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.print.PrintManager
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.PrintDocumentAdapter
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ProcessingDialogBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.CustomPrintingMonitorAdapter
import com.airprint.smart.printer.utils.PdfDocumentAdapter
import com.airprint.smart.printer.utils.PrintStatus
import com.airprint.smart.printer.utils.customToast
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    val TAG="BaseActivity"
   private  lateinit var originalContext: Context
   private var firebaseAnalytics: FirebaseAnalytics? = null
    protected val binding: VB by lazy {
        setupViewBinding().also {
            setContentView(it.root)
        }
    }
    private val dialog: Dialog by lazy {
        Dialog(this).apply {
            val dialogBinding = ProcessingDialogBinding.inflate(layoutInflater)
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val window = window
            val layoutParams = window?.attributes
            layoutParams?.gravity = Gravity.CENTER
            layoutParams?.width = (binding.root.width - resources.getDimension(com.intuit.sdp.R.dimen._15sdp)).toInt()
            layoutParams?.y = resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt()

            window?.attributes = layoutParams
            setCancelable(false)
        }
    }
    private lateinit var animatorSetEnableBtn: AnimatorSet
    private lateinit var scaleX: ObjectAnimator
    private lateinit var scaleY: ObjectAnimator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mainApplication: MyApplicationClass? =
            if (application is MyApplicationClass) application as MyApplicationClass else null
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)


    }
    abstract fun setupViewBinding(): VB




    fun setUpAnimationText(view: View){
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
    override fun attachBaseContext(newBase: Context) {
        originalContext = newBase
        try {
            val context = updateLanguage(newBase) ?: newBase
            super.attachBaseContext(context)
        } catch (e: Exception) {
            e.printStackTrace()
            super.attachBaseContext(newBase)
        }
    }

    fun printPdf(pdfFile: File) {
        try {
            val printManager = originalContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = "${getString(R.string.app_name)} Document"
                val printAdapter = com.airprint.smart.printer.adapter.PdfDocumentAdapter(pdfFile)
                printManager.print(jobName, printAdapter, null)
                dialog.dismiss()
            } else {
                Log.e("PrintError", "PrintManager is not available.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PrintError", "Failed to print: ${e.message}")
        }
    }
    fun updateLanguage(context: Context): Context? {
        return SharedPreferenceHelper.getString(context, Constent.LANG_CODE)
            ?.let { setAppLanguage(context, it) }
    }
    private fun setAppLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }


    fun getPrint(pdfUri: Uri) {
        val printManager =getPrintManager()
        val jobName = getString(R.string.app_name) + ": " + pdfUri.lastPathSegment
        val printAdapter = PdfDocumentAdapter(this, pdfUri)

        val printJob = printManager!!.print(jobName, printAdapter, null)
        val monitorAdapter = CustomPrintingMonitorAdapter()
        monitorAdapter.monitorPrintJobStatus(printJob)
    }



    fun getPrintManager(): PrintManager? {
        return try {
            originalContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

     fun fireLogEvent(id: String, name: String, type: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        firebaseAnalytics!!.logEvent(id, bundle)
    }
     fun fireLogEventExtra(id: String, name: String, type: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.TERM, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        firebaseAnalytics!!.logEvent(id, bundle)
    }


}