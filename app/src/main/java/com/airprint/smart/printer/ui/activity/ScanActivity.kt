package com.airprint.smart.printer.ui.activity

import android.Manifest
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.print.PrintManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airprint.smart.printer.BuildConfig
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.BaseAdapter
import com.airprint.smart.printer.adapter.PdfDocumentAdapter
import com.airprint.smart.printer.ads.AdsCallBack
import com.airprint.smart.printer.ads.InterstitialAdsManager
import com.airprint.smart.printer.ads.NativeAdManager
import com.airprint.smart.printer.ads.SharedPreferenceHelper
import com.airprint.smart.printer.databinding.ActivityScanBinding
import com.airprint.smart.printer.databinding.HistoryLayoutBinding
import com.airprint.smart.printer.databinding.ProcessingDialogBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.ControlConsent
import com.airprint.smart.printer.utils.DataStoreApi
import com.airprint.smart.printer.utils.IdesConstent
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.getCurrentFormattedDateTime
import com.airprint.smart.printer.utils.isCameraPermissionGranted
import com.airprint.smart.printer.utils.isInternetConnected
import com.airprint.smart.printer.utils.isNetworkAvailable
import com.airprint.smart.printer.utils.launchActivity
import com.airprint.smart.printer.utils.makeDialog
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.airprint.smart.printer.utils.uriToBitmap
import com.airprint.smart.printer.utils.uriToFile
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanActivity : BaseActivity<ActivityScanBinding>(), AdsCallBack {
    private val dialog: Dialog by lazy {
        Dialog(this).apply {
            val dialogBinding = ProcessingDialogBinding.inflate(layoutInflater)
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val window = window
            val layoutParams = window?.attributes
            layoutParams?.gravity = Gravity.CENTER
            layoutParams?.width = (binding.main.width - resources.getDimension(com.intuit.sdp.R.dimen._15sdp)).toInt()
            layoutParams?.y = resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt()
            window?.attributes = layoutParams
            setCancelable(false)
            dialogBinding.dialogTv.text= getString(R.string.text_extration)
        }
    }
    private var historyAdapter:BaseAdapter<DataStoreApi.HistoryModel,HistoryLayoutBinding>?=null
    private  var pickImageLauncher=registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            startCrop(it)
        }
    }
    private  var cropImageLauncher   = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val bitmap = result.getBitmap(this@ScanActivity)
            bitmap?.let {
                dialog.show()
                scanImage(
                    mediaImage = it,
                    onTextScanSuccess = { recognizedText ->
                        launchActivity<ClipBoardActivity> {
                            putExtra(Constent.OCR,recognizedText)
                        }
                        dialog.dismiss()
                        customToast(false,"Recognized Text: $recognizedText", 2000)
                    },
                    onScanError = { error ->
                        dialog.dismiss()
                        customToast(false,"Recognized Text: $error", 2000)
                    }
                )
            } ?: run {
                customToast(false, getString(R.string.failed_to_crop_image), 2000)
            }

        } else {
            val exception = result.error
            exception?.printStackTrace()
            customToast(false, getString(R.string.failed_to_crop_image),2000)

        }

    }


    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.let { pages ->
                if (pages.size == 1) {
                    val imageUri = pages[0].imageUri
                    processSelectedImage(imageUri)
                }
            }
            scanResult?.pdf?.let { pdf ->
                val pdfUri = pdf.uri
                val pageCount = pdf.pageCount
                if (pageCount > 1) {
                    val pdfFile = uriToFile(pdfUri)
                    pdfFile?.let {
                        printPdf(it)
                    }

                }
            }
        }
    }

    private val adDialog: Dialog by lazy { makeDialog() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nativeAds()

        binding.root.post {
            decideTheme()
            initView()
        }

    }

    private fun initView() {
        listener()
        setUpAdapter()
    }
    private fun nativeAds() {
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
    private fun setUpAdapter() {
        historyAdapter=object :BaseAdapter<DataStoreApi.HistoryModel,HistoryLayoutBinding>(){
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
                        DataStoreApi.deleteHistoryItem(this@ScanActivity, item)
                    }
                }
            }
        }
        binding.historyRcv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.historyRcv.adapter = historyAdapter
        historyAdapter?.setData(ArrayList())
        lifecycleScope.launch {
            DataStoreApi.getHistoryList(this@ScanActivity).collect { historyList ->
                if (historyList.isEmpty()) {
                   binding.historyLayout.visibility=View.GONE
                    binding.seeHistoryBtn.visibility=View.GONE
                } else {
                    binding.historyLayout.visibility=View.VISIBLE
                    binding.seeHistoryBtn.visibility=View.VISIBLE
                    historyAdapter?.setData(historyList as ArrayList<DataStoreApi.HistoryModel>)
                }
            }

        }

    }
    private fun scanImage(
            mediaImage: Bitmap,
            onTextScanSuccess: (String) -> Unit,
            onScanError: (String) -> Unit
        ) {
            val image = InputImage.fromBitmap(mediaImage, 90)
            val textDetector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            textDetector.process(image)
                .addOnSuccessListener {
                    it?.let { text ->
                        onTextScanSuccess(text.text)
                    }
                }
                .addOnFailureListener {
                    onScanError(it.message.toString())
                }
        }


    private fun listener() {
        binding.apply {
            scanDocumentsView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@ScanActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@ScanActivity, adId, isInternetConnected(),
                    this@ScanActivity, SharedPreferenceHelper.getBoolean(this@ScanActivity,
                        ControlConsent.SCAN_DOCUMENTS_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL), adDialog, Constent.ACTION_SCAN_DOCUMENTS)

            }
            documentsView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@ScanActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@ScanActivity, adId, isInternetConnected(),
                    this@ScanActivity, SharedPreferenceHelper.getBoolean(this@ScanActivity,
                        ControlConsent.QR_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL), adDialog, Constent.ACTION_SCAN_QR)
            }
            webpageView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@ScanActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@ScanActivity, adId, isInternetConnected(),
                    this@ScanActivity, SharedPreferenceHelper.getBoolean(this@ScanActivity,
                        ControlConsent.BAR_CODE_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL), adDialog, Constent.ACTION_SCAN_BAR_CODE)
            }
            emailsView.setSafeOnClickListener {
                val adId = SharedPreferenceHelper.getId(this@ScanActivity, IdesConstent.HOME_INTERSTITIAL_AD_ID)
                InterstitialAdsManager.loadInitInterstitial(this@ScanActivity, adId, isInternetConnected(),
                    this@ScanActivity, SharedPreferenceHelper.getBoolean(this@ScanActivity,
                        ControlConsent.TEXT_IN_CLICK_INTERSTITIAL_CONTROL_FROM_SCANNER_TOOL), adDialog, Constent.ACTION_TEXT_IN_IMAGE)
            }
            backBtn.setSafeOnClickListener {
                finish()
            }
            binding.seeHistoryBtn.setSafeOnClickListener {
                launchActivity<HistoryActivity>()
            }
            binding.seeAll.setSafeOnClickListener {
                launchActivity<HistoryActivity>()
            }


        }
    }
    private fun startWebpage(url: String, i: Int) {
        launchActivity<OpenWebPageActivity> {
            putExtra(Constent.TYPE_OF_URL,url)
            putExtra(Constent.SELECT_POS,i)
        }

    }
    private fun barCodeScan() {

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .build()
        val scanner = GmsBarcodeScanning.getClient(this@ScanActivity)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue: String? = barcode.rawValue
                rawValue?.let { scannedText ->
                    val newHistory = DataStoreApi.HistoryModel(
                        scanText = scannedText,
                        scanDate = getCurrentFormattedDateTime()
                    )
                    lifecycleScope.launch {
                        DataStoreApi.addHistory(this@ScanActivity, newHistory)
                    }
                }
            }
            .addOnCanceledListener {
                // Task canceled
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }


    }

    private fun scan() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF) //, RESULT_FORMAT_PDF
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)

        scanner.getStartScanIntent(this@ScanActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                it.message?.let { it1 -> customToast(false, it1,2000) }
            }
    }

    override fun setupViewBinding(): ActivityScanBinding {
        return ActivityScanBinding.inflate(layoutInflater)
    }


    private fun processSelectedImage(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = uriToBitmap(uri)
            val pdfFile = bitmap?.let { createPdfFileFromBitmap(this@ScanActivity, it) }
            if (pdfFile != null) {
                printPdf(pdfFile)
            }else{
                customToast(false,getString(R.string.printer_fail),2000)
            }
        }


    }

    private suspend fun createPdfFileFromBitmap(context: Context, bitmap: Bitmap): File? {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 0).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
            val pdfFile = File(context.cacheDir, "generated_pdf_${System.currentTimeMillis()}.pdf")

            try {
                FileOutputStream(pdfFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext null
            } finally {
                pdfDocument.close()
            }

            pdfFile
        }
    }


    private fun startCrop(uri: Uri) {

        val cropOptions = CropImageContractOptions(
            uri = uri,
            cropImageOptions = CropImageOptions().apply {
                fixAspectRatio = true
                guidelines = CropImageView.Guidelines.ON
                outputCompressFormat = Bitmap.CompressFormat.PNG
            }
        )
        cropImageLauncher.launch(cropOptions)
    }

    override fun onAdShow(string: String) {

    }

    override fun onAdClicked(string: String) {

    }

    override fun onAdDismiss(string: String) {
        decideWhichActionWillBeDone(string)
    }

    override fun onAdLoadToFail(string: String) {
        decideWhichActionWillBeDone(string)
    }

    override fun onAdShowToFail(string: String) {
        decideWhichActionWillBeDone(string)
    }

    override fun onAdImpression(string: String) {

    }
    private fun decideWhichActionWillBeDone(action: String) {
        when (action) {
            Constent.ACTION_SCAN_QR -> {
                barCodeScan()
            }
            Constent.ACTION_SCAN_BAR_CODE -> {
                barCodeScan()
            }
            Constent.ACTION_SCAN_DOCUMENTS -> {
                scan()
            }
            Constent.ACTION_TEXT_IN_IMAGE -> {
                pickImageLauncher.launch("image/*")
            }

        }
    }


}