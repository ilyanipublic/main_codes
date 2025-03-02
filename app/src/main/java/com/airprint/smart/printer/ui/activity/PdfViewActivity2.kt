package com.airprint.smart.printer.ui.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.BaseAdapter
import com.airprint.smart.printer.adapter.PdfDocumentAdapter
import com.airprint.smart.printer.databinding.ActivityPdfView2Binding
import com.airprint.smart.printer.databinding.ImageItemLayoutBinding
import com.airprint.smart.printer.databinding.ProcessingDialogBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.CustomDialog
import com.airprint.smart.printer.utils.ImageToPdfUtils
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.getBitmapFromView
import com.airprint.smart.printer.utils.printPdf
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.button.MaterialButton


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import kotlin.math.log

class PdfViewActivity2 : BaseActivity<ActivityPdfView2Binding>() {
    private val bitmapMap = mutableMapOf<Int, Bitmap>()
    private var imageUris: ArrayList<Uri>?=null
    private var hasScrolledToEndInitially = false
    private var imageAdapter:BaseAdapter<Uri,ImageItemLayoutBinding>?=null
    override fun setupViewBinding(): ActivityPdfView2Binding {
        return ActivityPdfView2Binding.inflate(layoutInflater)
    }
    private val dialog: Dialog by lazy {
        Dialog(this@PdfViewActivity2).apply {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decideTheme()
        inItView()
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("filter_string"))

    }
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }
    private fun inItView() {
         imageUris = intent.getParcelableArrayListExtra<Uri>(Constent.IMAGE_URI)
         imageUris?.let {
            setImageAdapter(it)
        }
        listener()


    }

    private fun listener() {
        binding.backBtn.setSafeOnClickListener {
            finish()
        }

        binding.continueBtn2.setSafeOnClickListener {
            dialog.show()
            lifecycleScope.launch(Dispatchers.IO) {
                val pdfFile = createPdfFileFromBitmaps(this@PdfViewActivity2, bitmapMap)
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



    }



    private fun setImageAdapter(uriList: ArrayList<Uri>) {
        imageAdapter = object : BaseAdapter<Uri, ImageItemLayoutBinding>() {
            override fun createBinding(
                inflater: LayoutInflater,
                parent: ViewGroup
            ): ImageItemLayoutBinding {
                return ImageItemLayoutBinding.inflate(inflater, parent, false)
            }

            override fun bind(imageItemLayoutBinding: ImageItemLayoutBinding, item: Uri, position: Int) {
                imageItemLayoutBinding.progressBar.visibility = View.VISIBLE
                Glide.with(this@PdfViewActivity2)
                    .load(item)
                    .placeholder(R.drawable.all_bg_dialog)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            imageItemLayoutBinding.progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (!bitmapMap.containsKey(position)) {
                                    withContext(Dispatchers.Main) {
                                        // Additional UI-related work if needed
                                    }
                                    bitmapMap[position] = imageItemLayoutBinding.IVImage.getBitmapFromView()
                                }
                            }
                            imageItemLayoutBinding.progressBar.visibility = View.GONE
                            return false
                        }
                    }).into(imageItemLayoutBinding.IVImage)

                binding.counterText.text = "${position + 1}/${uriList.size}"

                imageItemLayoutBinding.closeIcon.setSafeOnClickListener {
                    uriList.remove(item)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, uriList.size)
                }
            }
        }

        binding.imageRcv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.imageRcv.adapter = imageAdapter
        imageAdapter?.setData(uriList)

        // Initially scroll to the last position
        binding.imageRcv.scrollToPosition(uriList.size - 1)

        // Add a small delay to scroll back to the first position
        Handler(Looper.getMainLooper()).postDelayed({
            binding.imageRcv.smoothScrollToPosition(0)
            hasScrolledToEndInitially = true // Set the flag to true after the initial scroll sequence
        }, 500) // Adjust delay as needed (in milliseconds)
    }


private suspend fun createPdfFileFromBitmaps(context: Context, bitmapMap: Map<Int, Bitmap>): File? {
    return withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        var pageCount = 0
        bitmapMap.forEach { (_, bitmap) ->
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageCount).create()
            val page = pdfDocument.startPage(pageInfo)
            pageCount++

            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            pdfDocument.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "generated_pdf_${System.currentTimeMillis()}.pdf")

        try {
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null // Return null if saving fails
        } finally {
            pdfDocument.close()
        }

        pdfFile
    }
}



}