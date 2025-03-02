package com.airprint.smart.printer.ads

interface AdsCallBack {
    fun onAdShow(string: String)
    fun onAdClicked(string: String)
    fun onAdDismiss(string: String)
    fun onAdLoadToFail(string: String)
    fun onAdShowToFail(string: String)
    fun onAdImpression(string: String)
}