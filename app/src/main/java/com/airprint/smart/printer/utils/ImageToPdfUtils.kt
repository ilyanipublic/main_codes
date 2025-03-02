package com.airprint.smart.printer.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.widget.Toast
import java.io.File
import java.io.IOException
import android.graphics.pdf.PdfDocument;
import java.io.FileOutputStream

object ImageToPdfUtils {
    fun createImagesToPDF(context: Context, imagePaths: List<String>): File? {
        val pdfDocument = PdfDocument()
        for ((index, imagePath) in imagePaths.withIndex()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            bitmap?.let {
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply { color = Color.WHITE }
                canvas.drawPaint(paint)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
            }
        }

        val pdfFile = File(context.filesDir, Constent.PDF_FILE_NAME)

        return try {
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            pdfFile
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

}