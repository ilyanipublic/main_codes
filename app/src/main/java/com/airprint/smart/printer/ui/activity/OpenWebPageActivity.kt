package com.airprint.smart.printer.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.airprint.smart.printer.R
import com.airprint.smart.printer.databinding.ActivityOpenWebPageBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.Constent.PRINTER_DELAY
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.isInternetConnected
import com.airprint.smart.printer.utils.setSafeOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OpenWebPageActivity : BaseActivity<ActivityOpenWebPageBinding>() {
    private var webUrl:String?=null
    private var selectedPos=0

    override fun setupViewBinding(): ActivityOpenWebPageBinding {
        return ActivityOpenWebPageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decideTheme()
        initViews()
        binding.continueBtn.setSafeOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val printManager = getPrintManager()
                val jobName = "${resources.getString(R.string.app_name)} Document"
                val printAdapter = binding.webView.createPrintDocumentAdapter(jobName)
                delay(PRINTER_DELAY)
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }

        }
    }

    private fun initViews() {
        webUrl = intent.getStringExtra(Constent.TYPE_OF_URL)
        selectedPos = intent.getIntExtra(Constent.SELECT_POS, 0)
        loadPage()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadPage() {
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = WebViewClient()

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true

        if (isInternetConnected()){
            binding.progressBar.visibility = View.VISIBLE
            webUrl?.let { binding.webView.loadUrl(it) }
        }else{
            binding.progressBar.visibility = View.GONE
            customToast(false,getString(R.string.check_internet),2000)
        }



    }


}