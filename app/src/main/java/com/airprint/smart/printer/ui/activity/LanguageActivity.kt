package com.airprint.smart.printer.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.BaseAdapter
import com.airprint.smart.printer.ads.NativeAdManager
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityLanguageBinding
import com.airprint.smart.printer.databinding.LanguageItemLayoutBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.ControlConsent
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.LanguageModel
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.getPredefinedLanguages
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.setSafeOnClickListener

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private var selectedPos = 0
    private var languageCode = ""
    private var languageAdapter: BaseAdapter<LanguageModel, LanguageItemLayoutBinding>? = null
    override fun setupViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decideTheme()
        nativeAds()
        initView()
    }

    private fun initView() {
        binding.backBtn.setSafeOnClickListener {
            finish()
        }
        binding.doneBtn.setSafeOnClickListener {
            SharedPreferenceHelper.setString(this@LanguageActivity, Constent.LANG_CODE, languageCode)
            SharedPreferenceHelper.setInt(this@LanguageActivity, Constent.LANG_POS, selectedPos)
             updateLanguage(this@LanguageActivity)
            launchActivity<MainActivity>()
            finish()
        }
        selectedPos=SharedPreferenceHelper.getInt(this@LanguageActivity,Constent.LANG_POS)
        setUpAdapter()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpAdapter() {
        val languageList = emptyList<LanguageModel>().getPredefinedLanguages()

        languageAdapter = object : BaseAdapter<LanguageModel, LanguageItemLayoutBinding>() {
            override fun createBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): LanguageItemLayoutBinding {
                return LanguageItemLayoutBinding.inflate(layoutInflater, parent, false)
            }

            override fun bind(
                binding: LanguageItemLayoutBinding,
                item: LanguageModel,
                position: Int
            ) {
                item.flag?.let { binding.flag.setImageResource(it) }
                binding.languageName.text = item.languageName
                if (selectedPos == position) {
                    binding.languageSelected.setImageResource(R.drawable.selected_icon)
                } else {
                    binding.languageSelected.setImageResource(R.drawable.un_selected_icon)
                }
            }
        }
        languageAdapter?.setOnItemClickListener { languageItemLayoutBinding, languageModel, i ->
            selectedPos=i
            languageCode=languageModel.languageCode!!
            languageAdapter?.notifyDataSetChanged()
        }

        binding.rcvLanguage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvLanguage.adapter = languageAdapter
        languageAdapter?.setData(ArrayList(languageList))
    }

    private fun nativeAds() {
        if (/*SharedPreferenceHelper.getBoolean(this, ControlConsent.LANGUGE_NATIVE_CONTROL) &&*/ isNetworkAvailable()) {
            val adUnit = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                SharedPreferenceHelper.getString(this, IdesConstent.LANGUAGE_NATIVE_ID)
            }
            adUnit?.let {
                NativeAdManager.requestAdmobNativeAd(
                    this,
                    it,
                    binding.nativeAdLayout,
                    binding.loadNative, Constent.LARGE_NATIVE_AD
                )
            }


        } else {
            binding.layoutAd.visibility = View.GONE
        }
    }


}