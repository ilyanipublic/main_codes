package com.airprint.smart.printer.ui.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.print.PrintManager
import android.view.Gravity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.PdfDocumentAdapter
import com.airprint.smart.printer.databinding.ActivityImageCropBinding
import com.airprint.smart.printer.databinding.ProcessingDialogBinding
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCropActivity : BaseActivity<ActivityImageCropBinding>() {

    companion object {
        private const val FILE_DIR = "FileDir"
        fun newIntent(context: Context, selectedFilePath: String) =
            Intent(context, ImageCropActivity::class.java).putExtra(FILE_DIR, selectedFilePath)
    }
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
        }
    }

    override fun setupViewBinding(): ActivityImageCropBinding {
        return ActivityImageCropBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decideTheme()
        val bitmap = assetToBitmap(intent.extras?.getString(FILE_DIR)!!)
      /*  binding.documentScanner.setOnLoadListener { loading ->
            binding.progressBar.isVisible = loading
        }
        binding.documentScanner.setImage(bitmap)
        binding.continueBtn2.setOnClickListener {
            lifecycleScope.launch {
                binding.progressBar.isVisible = true
                val image = binding.documentScanner.getCroppedImage()
                binding.progressBar.isVisible = false
                binding.resultImage.isVisible = true
                dialog.show()
                lifecycleScope.launch(Dispatchers.IO) {
                    val pdfFile = createPdfFileFromBitmap(this@ImageCropActivity, image)
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        if (pdfFile != null) {
                            printPdf(pdfFile)
                        } else {
                            customToast(false, "Something went wrong", 2000)
                        }
                    }
                }

            }
        }*/
    }
    private fun assetToBitmap(file: String): Bitmap =
        contentResolver.openInputStream(Uri.parse(file)).run {
            BitmapFactory.decodeStream(this)
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



}