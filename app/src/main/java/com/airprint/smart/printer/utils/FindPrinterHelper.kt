package com.airprint.smart.printer.utils

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper


class FindPrinterHelper(val context: Context, private val listener: PrinterResultListener) {

    interface PrinterResultListener {
        fun onPrinterFound(printer: NsdServiceInfo)
        fun onPrinterLost(printer: NsdServiceInfo)
        fun onDiscoveryStopped(noPrintersFound: Boolean)
    }

    private var nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null
    private val printers = mutableListOf<NsdServiceInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private val discoveryTimeout = 10000L // 10 seconds timeout

    fun initializeDiscoveryListener() {
        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                println("Service discovery started")
                // Start the timeout countdown
                handler.postDelayed({
                    stopDiscovery()
                    listener.onDiscoveryStopped(printers.isEmpty())
                }, discoveryTimeout)
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                println("Service discovery success: $service")
                if (service.serviceType.contains("_ipp._tcp") || service.serviceType.contains("_printer._tcp")) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                println("Service lost: $service")
                printers.remove(service)
                listener.onPrinterLost(service)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                println("Discovery stopped: $serviceType")
                listener.onDiscoveryStopped(printers.isEmpty())
                handler.removeCallbacksAndMessages(null) // Remove timeout callback
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                println("Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
                handler.removeCallbacksAndMessages(null) // Remove timeout callback
                listener.onDiscoveryStopped(true)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                println("Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
                handler.removeCallbacksAndMessages(null) // Remove timeout callback
                listener.onDiscoveryStopped(true)
            }
        }
    }

    fun startDiscovery() {
        try {
            nsdManager.discoverServices("_ipp._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }catch (_: Exception){}
    }

    fun stopDiscovery() {
        try {
            discoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
        }catch (_: Exception){}

        try {
            handler.removeCallbacksAndMessages(null) // Remove timeout callback
        }catch (_: Exception){}
    }

    fun getDiscoveredPrinters(): List<NsdServiceInfo> {
        return printers
    }

    private fun resolveService(service: NsdServiceInfo) {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                println("Resolve failed: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                println("Resolve Succeeded. $serviceInfo")
                printers.add(serviceInfo)
                listener.onPrinterFound(serviceInfo)
            }
        }

        nsdManager.resolveService(service, resolveListener)
    }


}