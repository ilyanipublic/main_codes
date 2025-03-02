package com.airprint.smart.printer.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.BaseAdapter
import com.airprint.smart.printer.ads.NativeAdManager
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityHistoryBinding
import com.airprint.smart.printer.databinding.HistoryLayoutBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.DataStoreApi
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.setSafeOnClickListener
import kotlinx.coroutines.launch

class HistoryActivity : BaseActivity<ActivityHistoryBinding>() {
    private var historyAdapter:BaseAdapter<DataStoreApi.HistoryModel,HistoryLayoutBinding>?=null
    private var isAdLoad=true
    override fun setupViewBinding(): ActivityHistoryBinding {
        return ActivityHistoryBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.post {
            decideTheme()
            setUpAdapter()
        }
    }

    private fun setUpAdapter() {
        binding.clearAll.setSafeOnClickListener {
            lifecycleScope.launch {
                DataStoreApi.clearHistory(this@HistoryActivity)
                setUpAdapter()
                customToast(true, getString(R.string.delete_your_all_history_successfully),2000)
                finish()
            }
        }
        binding.backBtn.setSafeOnClickListener {
            finish()
        }
        historyAdapter=object : BaseAdapter<DataStoreApi.HistoryModel, HistoryLayoutBinding>(){
            override fun createBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): HistoryLayoutBinding {
                return HistoryLayoutBinding.inflate(inflater, parent, false)
            }

            override fun bind(
                binding: HistoryLayoutBinding,
                item: DataStoreApi.HistoryModel,
                position: Int
            ) {
                binding.linkTv.paintFlags =  binding.linkTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                binding.linkTv.text=item.scanText
                binding.dateTv.text=item.scanDate
                binding.linkTv.setSafeOnClickListener {
                    item.scanText?.let { it1 -> startWebpage(it1, Constent.GOOGLE_URL_TAG) }
                }
                binding.copyIcon.setSafeOnClickListener {
                    if (!item.scanText.isNullOrEmpty()) {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Text", item.scanText)
                        clipboard.setPrimaryClip(clip)
                    } else {
                        runOnUiThread {
                            customToast(false, getString(R.string.no_text_to_copy), 2000)
                        }
                    }
                }
                binding.deleteIcon.setSafeOnClickListener {
                    lifecycleScope.launch {
                        DataStoreApi.deleteHistoryItem(this@HistoryActivity, item)
                    }
                }




            }
        }
        binding.historyRcv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.historyRcv.adapter = historyAdapter
        historyAdapter?.setData(ArrayList())
        lifecycleScope.launch {
            DataStoreApi.getHistoryList(this@HistoryActivity).collect { historyList ->
                if (historyList.isNotEmpty()) {
                    if (isAdLoad){
                        nativeAds()
                        isAdLoad=false
                    }
                    historyAdapter?.setData(historyList as ArrayList<DataStoreApi.HistoryModel>)
                }
            }

        }

    }
    private fun startWebpage(url: String, i: Int) {
        launchActivity<OpenWebPageActivity> {
            putExtra(Constent.TYPE_OF_URL,url)
            putExtra(Constent.SELECT_POS,i)
        }

    }

    private fun nativeAds() {
        Log.d(TAG, "nativeAds: how many time")
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

}