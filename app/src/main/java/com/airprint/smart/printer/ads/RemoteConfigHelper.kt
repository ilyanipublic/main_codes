package com.airprint.smart.printer.ads

import android.content.Context
import android.util.Log
import com.airprint.smart.printer.utils.ControlConsent.BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL
import com.airprint.smart.printer.utils.ControlConsent.CLIP_BOARD_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.CONNECTIVITY_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.DOCUMENTS_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.EMAIL_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.HOME_NATIVE_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.INTRO_NATIVE_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.LANGUGE_NATIVE_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.PHOTO_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.QR_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL
import com.airprint.smart.printer.utils.ControlConsent.SCANNER_TOOL_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.SCAN_ALL_NATIVE_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.SCAN_DOCUMENTS_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL
import com.airprint.smart.printer.utils.ControlConsent.SPLASH_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.ControlConsent.TEXT_IN_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL
import com.airprint.smart.printer.utils.ControlConsent.WEBPAGE_CLICK_INTERSTITIAL_CONTROL
import com.airprint.smart.printer.utils.IdesConstent.INTRO_NATIVE_ID
import com.airprint.smart.printer.utils.IdesConstent.LANGUAGE_NATIVE_ID
import com.airprint.smart.printer.utils.IdesConstent.SPLASH_INTERSTITIAL_AD_ID
import com.google.firebase.remoteconfig.ktx.BuildConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig


class RemoteConfigHelper {
    companion object {
        const val TAG = "FirebaseRemoteConfig"

        fun fireBaseRemoteFetch(activity: Context, remoteConfigCallBack: RemoteConfigCallBack?) {
            val remoteConfig = com.google.firebase.ktx.Firebase.remoteConfig
            val configSettings = com.google.firebase.remoteconfig.ktx.remoteConfigSettings {
                minimumFetchIntervalInSeconds = 1L
                fetchTimeoutInSeconds = 40L
            }

            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "isSuccessful----------->")
                        //.............................ALL_IDS.........................................................

                        if (BuildConfig.DEBUG){
                            SharedPreferenceHelper.setString(activity, SPLASH_INTERSTITIAL_AD_ID, remoteConfig.getString(SPLASH_INTERSTITIAL_AD_ID))
                            SharedPreferenceHelper.setString(activity, INTRO_NATIVE_ID, remoteConfig.getString(INTRO_NATIVE_ID))
                            SharedPreferenceHelper.setString(activity, LANGUAGE_NATIVE_ID, remoteConfig.getString(LANGUAGE_NATIVE_ID))

                        }else{
                            //...........live id
                            SharedPreferenceHelper.setString(activity, SPLASH_INTERSTITIAL_AD_ID, remoteConfig.getString(SPLASH_INTERSTITIAL_AD_ID))
                            SharedPreferenceHelper.setString(activity, INTRO_NATIVE_ID, remoteConfig.getString(INTRO_NATIVE_ID))
                            SharedPreferenceHelper.setString(activity, LANGUAGE_NATIVE_ID, remoteConfig.getString(LANGUAGE_NATIVE_ID))

                        }


                        //.................................INTERSTITIAL_CONTROL.........................................
                        SharedPreferenceHelper.setBoolean(activity, SPLASH_INTERSTITIAL_CONTROL,true /*remoteConfig.getBoolean(SPLASH_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, CONNECTIVITY_CLICK_INTERSTITIAL_CONTROL,true /*remoteConfig.getBoolean(CONNECTIVITY_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, CLIP_BOARD_CLICK_INTERSTITIAL_CONTROL,true/* remoteConfig.getBoolean(CLIP_BOARD_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, PHOTO_CLICK_INTERSTITIAL_CONTROL, true/*remoteConfig.getBoolean(PHOTO_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, DOCUMENTS_CLICK_INTERSTITIAL_CONTROL, true/*remoteConfig.getBoolean(DOCUMENTS_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, WEBPAGE_CLICK_INTERSTITIAL_CONTROL,true/* remoteConfig.getBoolean(WEBPAGE_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, SCANNER_TOOL_CLICK_INTERSTITIAL_CONTROL,true/* remoteConfig.getBoolean(SCANNER_TOOL_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, SCAN_DOCUMENTS_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL,true /*remoteConfig.getBoolean(SCAN_DOCUMENTS_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL)*/)
                        SharedPreferenceHelper.setBoolean(activity, EMAIL_CLICK_INTERSTITIAL_CONTROL,true/* remoteConfig.getBoolean(EMAIL_CLICK_INTERSTITIAL_CONTROL)*/)
                        SharedPreferenceHelper.setBoolean(activity, TEXT_IN_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL,true /*remoteConfig.getBoolean(TEXT_IN_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL)*/)
                        SharedPreferenceHelper.setBoolean(activity, QR_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL, true/*remoteConfig.getBoolean(QR_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL)*/)
                        SharedPreferenceHelper.setBoolean(activity, BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL,true/* remoteConfig.getBoolean(BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL)*/)
                        SharedPreferenceHelper.setBoolean(activity, BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL, true/*remoteConfig.getBoolean(BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL)*/)

                        //.............................ALL NATIVE CONTROL.........................................................
                        SharedPreferenceHelper.setBoolean(activity, INTRO_NATIVE_CONTROL, remoteConfig.getBoolean(INTRO_NATIVE_CONTROL))
                        SharedPreferenceHelper.setBoolean(activity, LANGUGE_NATIVE_CONTROL, remoteConfig.getBoolean(LANGUGE_NATIVE_CONTROL))
                        SharedPreferenceHelper.setBoolean(activity, HOME_NATIVE_CONTROL, remoteConfig.getBoolean(HOME_NATIVE_CONTROL))
                        SharedPreferenceHelper.setBoolean(activity, SCAN_ALL_NATIVE_CONTROL, remoteConfig.getBoolean(SCAN_ALL_NATIVE_CONTROL))




                        remoteConfigCallBack?.onRemoteSuccess()
                    } else {
                        Log.d(TAG, "fail--------->")
                        remoteConfigCallBack?.onRemoteFailed()
                    }
                }

        }
    }
}