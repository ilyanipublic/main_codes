package com.airprint.smart.printer.ui.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.print.PrintManager
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.airprint.smart.printer.R
import com.airprint.smart.printer.adapter.PdfDocumentAdapter
import com.airprint.smart.printer.adapter.PrintDocumentAdapter
import com.airprint.smart.printer.databinding.ActivityClipBoardBinding
import com.airprint.smart.printer.databinding.ProcessingDialogBinding
import com.airprint.smart.printer.utils.Constent
import com.airprint.smart.printer.utils.Constent.PRINTER_DELAY
import com.airprint.smart.printer.utils.CustomPrintingMonitorAdapter
import com.airprint.smart.printer.utils.PrintStatus
import com.airprint.smart.printer.utils.customToast
import com.airprint.smart.printer.utils.decideTheme
import com.airprint.smart.printer.utils.setSafeOnClickListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File



class ClipBoardActivity : BaseActivity<ActivityClipBoardBinding>() {
    private var isBold = false
    private var isAlignActive = false
    private var isBulletAlign = false
    private var ttButton = false
    private  var isItalic = false
    private var isBulletSpanEnabled = false
    private var isNumberBulletEnable = false

    private val dialog: Dialog by lazy {
        Dialog(this@ClipBoardActivity).apply {
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
    override fun setupViewBinding(): ActivityClipBoardBinding {
        return ActivityClipBoardBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.post {
            decideTheme()
            initView()
        }
    }


    private fun initView() {
        val ocrText=intent.getStringExtra(Constent.OCR)
        binding.eTv.setText(ocrText)
        listener()
        setBulletNumber()

    }


    private fun listener() {
        binding.apply {
            backBtn.setSafeOnClickListener {
                finish()
            }
            continueBtn2.setSafeOnClickListener {

                val textContent = eTv.text.toString()
                if (textContent.isNotEmpty()) {
                    dialog.show()

                    printText(textContent)
                }else{
                    customToast(false,"Please add text",2000)
                }
            }
            undoBtn.setSafeOnClickListener {

            }
            redoBtn.setSafeOnClickListener {

            }
            //....................1ST LAYOUT...................
            boldBtn.setOnClickListener {
                isBold = !isBold // Toggle the bold state
                updateTextStyle()
            }
            italicBtn.setOnClickListener {
                isItalic = !isItalic // Toggle the italic state
                updateTextStyle()
            }
            underLineBtn.setOnClickListener {
                val spannableString = SpannableStringBuilder(binding.eTv.text.toString().trim())
                spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.eTv.text = spannableString
            }
            textAlignBtn.setSafeOnClickListener {
                if (isAlignActive){
                    textAlignBtn.setImageResource(R.drawable.ic_text_align)
                    moreOptionLayoutSecond.visibility= View.GONE
                    isAlignActive=false
                }else{
                    textAlignBtn.setImageResource(R.drawable.down)
                    isAlignActive=true
                    moreOptionLayoutSecond.visibility= View.VISIBLE
                }


            }
            textBulletAlign.setSafeOnClickListener {
                if (isBulletAlign){
                    textBulletAlign.setImageResource(R.drawable.ic_bullet)
                    moreOptionLayoutThird.visibility= View.GONE
                    isBulletAlign=false
                }else{
                    textBulletAlign.setImageResource(R.drawable.up_)
                    isBulletAlign=true
                    moreOptionLayoutThird.visibility= View.VISIBLE
                }


            }
            //....................2ND LAYOUT...................
            leftBtn.setSafeOnClickListener {
                resetButtonColors()
                leftBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.eTv.gravity = Gravity.START
            }
            centerBtn.setSafeOnClickListener {
                resetButtonColors()
                centerBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.eTv.gravity = Gravity.CENTER_HORIZONTAL
            }
            rightBtn.setSafeOnClickListener {
                resetButtonColors()
                rightBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)
                binding.eTv.gravity = Gravity.END
            }
            equalBtn.setSafeOnClickListener {
                resetButtonColors()
                equalBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)
                //binding.eTv.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            }
            //....................3RD LAYOUT...................
            ttBtn.setSafeOnClickListener {
                val text = binding.eTv.text.toString()
                binding.eTv.setText(if (text == text.uppercase()) text.lowercase() else text.uppercase())
                resetFilterColors()
                ttBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)
            }
            dollarSignBtn.setSafeOnClickListener {
                val cursorPos = binding.eTv.selectionStart
                val text = binding.eTv.text
                text?.insert(cursorPos, "$")
                binding.eTv.setSelection(cursorPos + 1)
                resetFilterColors()
                dollarSignBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)

            }
            countBtn.setSafeOnClickListener {
                isBulletSpanEnabled = false
                if (isNumberBulletEnable) {
                    binding.eTv.setText(removeBulletNumbers(binding.eTv.text.toString()))
                    resetFilterColors()
                    isNumberBulletEnable = false
                } else {
                    binding.eTv.setText(removeBullets(binding.eTv.text.toString()))
                    setBulletNumber(binding.eTv)
                    resetFilterColors()
                    countBtn.setColorFilter(
                        ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    isNumberBulletEnable = true
                }
            }

            bolBtn.setSafeOnClickListener {
                isNumberBulletEnable = false
                if (isBulletSpanEnabled) {
                    binding.eTv.setText(removeBullets(binding.eTv.text.toString()))
                    bolBtn.clearColorFilter()
                    isBulletSpanEnabled = false
                } else {
                    binding.eTv.setText(removeBulletNumbers(binding.eTv.text.toString()))
                    resetFilterColors()
                    bolBtn.setColorFilter(
                        ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    addBulletsToExistingText()
                    setEnterBullet()
                    isBulletSpanEnabled = true
                }
            }


            leftErrowBtn.setSafeOnClickListener {

                val cursorPos = binding.eTv.selectionStart
                if (cursorPos > 0) binding.eTv.setSelection(cursorPos - 1)
                resetFilterColors()
                leftErrowBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)

            }
            rightErrowBtn.setSafeOnClickListener {
                val cursorPos = binding.eTv.selectionStart
                if (cursorPos < binding.eTv.text!!.length) binding.eTv.setSelection(cursorPos + 1)
                resetFilterColors()
                rightErrowBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.blue_color), android.graphics.PorterDuff.Mode.SRC_IN)

            }
        }
    }

    private fun setEnterBullet() {
        binding.eTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) {
                // No implementation needed
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text != null && count > before) {
                    if (text.endsWith("\n")) {
                        var updatedText = text.toString().replace("\n", "\n◎ ")
                        updatedText = updatedText.replace("◎ ◎", "◎") // Avoid duplicate bullets
                        binding.eTv.setText(updatedText)
                        binding.eTv.setSelection(binding.eTv.text!!.length)
                    }
                }
            }
        })
    }
    private fun removeBullets(text: String): String {
        val lines = text.split("\n")
        return lines.joinToString("\n") { line ->
            line.replace(Regex("^◎\\s"), "") // Remove bullet prefix
        }
    }
    private fun setBulletNumber() {
        binding.eTv.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                if (isNumberBulletEnable) {
                    addNextNumber(binding.eTv)
                    return@setOnKeyListener true
                }
            }
            false
        }

    }
    private fun addBulletsToExistingText() {
        val currentText = binding.eTv.text.toString()
        val updatedText = currentText.split("\n").joinToString("\n") { line ->
            val cleanedLine = if (line.startsWith("◎ ") || Regex("^\\d+\\.\\s").containsMatchIn(line)) {
                line.replace(Regex("^◎\\s|^\\d+\\.\\s"), "") // Remove existing bullet or number prefix
            } else {
                line
            }
            "◎ $cleanedLine" // Add bullet prefix
        }
        binding.eTv.setText(updatedText)
        binding.eTv.setSelection(binding.eTv.text!!.length) // Move cursor to the end
    }
    private fun setBulletNumber(editText: EditText) {
        val currentText = editText.text.toString()
        val lines = currentText.split("\n")
        val numberedText = lines.mapIndexed { index, line ->
            val cleanedLine = if (line.startsWith("◎ ") || Regex("^\\d+\\.\\s").containsMatchIn(line)) {
                line.replace(Regex("^◎\\s|^\\d+\\.\\s"), "") // Remove existing bullet or number prefix
            } else {
                line
            }
            "${index + 1}. $cleanedLine" // Add numbered prefix
        }.joinToString("\n")
        editText.setText(numberedText)
        editText.setSelection(editText.text.length) // Move cursor to the end
    }
    private fun removeBulletNumbers(text: String): String {
        val lines = text.split("\n")
        val originalText = lines.map { line ->
            // Remove the number prefix (e.g., "1. ", "2. ") using regex
            line.replace(Regex("^\\d+\\.\\s"), "")
        }.joinToString("\n")
        return originalText
    }
    private fun addNextNumber(editText: EditText) {
        val currentText = editText.text.toString()
        val lines = currentText.split("\n")
        val nextNumber = lines.size + 1 // Calculate the next number
        editText.append("\n$nextNumber. ")
        editText.setSelection(editText.text.length) // Move cursor to the end
    }






    private fun resetFilterColors() {
        binding.apply {
            ttBtn.clearColorFilter()
            dollarSignBtn.clearColorFilter()
            countBtn.clearColorFilter()
            bolBtn.clearColorFilter()
            leftErrowBtn.clearColorFilter()
            rightErrowBtn.clearColorFilter()
        }
    }

    private fun updateTextStyle() {
        binding.eTv.setTypeface(null, when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        })
    }
    private fun resetButtonColors() {
        binding.apply {
            leftBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.find_printer_color), android.graphics.PorterDuff.Mode.SRC_IN)
            centerBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.find_printer_color), android.graphics.PorterDuff.Mode.SRC_IN)
            rightBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.find_printer_color), android.graphics.PorterDuff.Mode.SRC_IN)
            equalBtn.setColorFilter(ContextCompat.getColor(this@ClipBoardActivity, R.color.find_printer_color), android.graphics.PorterDuff.Mode.SRC_IN)

        }

    }


    private fun printText(text: String) {
        if (!isFinishing && !isDestroyed){
            CoroutineScope(Dispatchers.Main).launch {
                val printManager = getPrintManager()
                val jobName = "Document"
                val printDocumentAdapter = PrintDocumentAdapter(this@ClipBoardActivity, text)
                delay(PRINTER_DELAY)
                if (!isFinishing && !isDestroyed && printManager != null){
                    val printJob = printManager.print(jobName, printDocumentAdapter, null)
                    dialog.dismiss()

                    val monitorAdapter = CustomPrintingMonitorAdapter()
                    monitorAdapter.monitorPrintJobStatus(printJob)

                    monitorAdapter.printStatusFlow.collect { status ->
                        when (status) {
                            PrintStatus.IN_PROGRESS -> {

                                customToast(true, getString(R.string.preparing_printer),1500)
                            }
                            PrintStatus.COMPLETED -> {
                                if (!isFinishing && !isDestroyed){
                                    customToast(true, getString(R.string.printer_complete),2000)

                                }
                            }
                            PrintStatus.FAILED -> {
                                if (!isFinishing && !isDestroyed){
                                    customToast(false, getString(R.string.printer_fail),2000)
                                }
                            }
                            PrintStatus.CANCELED -> {
                                if (!isFinishing && !isDestroyed){
                                    customToast(false, getString(R.string.printer_cancel),2000)
                                }
                            }

                        }
                    }
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

}