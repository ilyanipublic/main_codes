package com.airprint.smart.printer.ui.activity

import android.os.Bundle
import com.airprint.smart.printer.databinding.ActivityAllScanerBinding
import java.util.concurrent.ExecutorService

class AllScanerActivity : BaseActivity<ActivityAllScanerBinding>() {
    private lateinit var cameraExecutor: ExecutorService
    override fun setupViewBinding(): ActivityAllScanerBinding {
        return ActivityAllScanerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //  initViews()
    }





}