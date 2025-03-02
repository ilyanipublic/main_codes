package com.airprint.smart.printer.ui.activity

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.BaseAdapter
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityConnectivityBinding
import com.airprint.smart.printer.databinding.ActivityMainBinding
import com.airprint.smart.printer.databinding.PrinterItemLayoutBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.FindPrinterHelper
import com.airprint.smart.printer.utils.connectToPrinter
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.setSafeOnClickListener

class ConnectivityActivity : BaseActivity<ActivityConnectivityBinding>(),FindPrinterHelper.PrinterResultListener {
    private lateinit var findPrinterHelper: FindPrinterHelper
    private val allPrinterList = mutableListOf<NsdServiceInfo>()
    private var printerAdapter:BaseAdapter<NsdServiceInfo,PrinterItemLayoutBinding>?=null
    override fun setupViewBinding(): ActivityConnectivityBinding {
        return ActivityConnectivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.post {
            decideTheme()
            inItViews()
        }
    }

    private fun inItViews() {
        binding.apply {
            backBtn.setSafeOnClickListener {
                finish()
            }
            guideBtn.setSafeOnClickListener {

            }
            guideBtn.setSafeOnClickListener {
                launchActivity<HelpCenterActivity>()
            }
        }
        makeConnection()
    }

    private fun makeConnection() {
        binding.connectionRipple.startRippleAnimation()
        findPrinterHelper = FindPrinterHelper(this, this)
        findPrinterHelper.initializeDiscoveryListener()
        findPrinterHelper.startDiscovery()

    }

    override fun onPrinterFound(printer: NsdServiceInfo) {
        if (allPrinterList.size>0){
            for (i in 0..< allPrinterList.size){
                if (allPrinterList[i].serviceType != printer.serviceType && allPrinterList[i].serviceName != printer.serviceName && allPrinterList[i].port != printer.port && allPrinterList[i].host != printer.host){
                    allPrinterList.add(printer)
                    runOnUiThread {
                        binding.connectionRipple.stopRippleAnimation()
                        setUpPrinterAdapter()
                    }

                }
            }
        } else {
            allPrinterList.add(printer)
            runOnUiThread {
                binding.connectionRipple.stopRippleAnimation()
                binding.printerNotFoundLayout.visibility=View.VISIBLE
                binding.printerFondLayout.visibility=View.GONE
                setUpPrinterAdapter()
            }

        }
    }

    private fun setUpPrinterAdapter() {
        printerAdapter=object :BaseAdapter<NsdServiceInfo,PrinterItemLayoutBinding>(){
            override fun createBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): PrinterItemLayoutBinding {
                return PrinterItemLayoutBinding.inflate(inflater, parent, false)
            }

            override fun bind(
                binding: PrinterItemLayoutBinding,
                item: NsdServiceInfo,
                position: Int
            ) {
                binding.printerNameTv.text=item.serviceName
                binding.printerTypeTv.text=item.serviceType
            }

        }
        printerAdapter?.setOnItemClickListener { printerItemLayoutBinding, nsdServiceInfo, i ->
            connectToSelectedPrinter(nsdServiceInfo)
        }
        binding.printerRCV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.printerRCV.adapter = printerAdapter
        printerAdapter?.setData(allPrinterList as ArrayList<NsdServiceInfo>)
    }

    override fun onPrinterLost(printer: NsdServiceInfo) {
        allPrinterList.remove(printer)
        runOnUiThread {
            binding.connectionRipple.stopRippleAnimation()
            setUpPrinterAdapter()
        }


    }

    override fun onDiscoveryStopped(noPrintersFound: Boolean) {
        runOnUiThread {
            if (noPrintersFound) {
                binding.connectionRipple.stopRippleAnimation()
                binding.printerNotFoundLayout.visibility=View.VISIBLE
                binding.printerFondLayout.visibility=View.GONE

            } else {
                setUpPrinterAdapter()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        try {
            findPrinterHelper.stopDiscovery()
        }catch (_: Exception){}
    }
    private fun connectToSelectedPrinter(printer: NsdServiceInfo) {
        printer.host.hostAddress?.let {
            connectToPrinter(it, printer.port, "Hello, printer!".toByteArray()){
                Toast.makeText(this, "${resources.getString(R.string.connected_to)} ${printer.serviceName}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

}