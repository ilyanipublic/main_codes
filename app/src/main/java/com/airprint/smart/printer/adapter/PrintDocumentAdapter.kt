package com.airprint.smart.printer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import java.io.IOException

class PrintDocumentAdapter (
    private val context: Context,
    private val text: String
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null
    private var pageHeight: Int = 0
    private var pageWidth: Int = 0
    private val printTextPaint = Paint()

    init {
        printTextPaint.textSize = 12f // Set your desired text size
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {

        newAttributes?.let {attributes ->
            pdfDocument = PrintedPdfDocument(context, attributes)

            pageHeight = attributes.mediaSize!!.heightMils / 1000 * 72
            pageWidth = attributes.mediaSize!!.widthMils / 1000 * 72

            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }

            val info = PrintDocumentInfo.Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()

            callback?.onLayoutFinished(info, true)
        } ?: run {
            callback?.onLayoutCancelled()
        }

    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        var pageNumber = 0
        var textIndex = 0
        var lineHeight = printTextPaint.fontMetrics.bottom - printTextPaint.fontMetrics.top
        val textLines = text.split("\n")

        // Create pages and write text on each
        while (textIndex < textLines.size) {
            val page = pdfDocument!!.startPage(pageNumber)
            if (cancellationSignal.isCanceled) {
                pdfDocument!!.close()
                callback.onWriteCancelled()
                return
            }

            val canvas: Canvas = page.canvas
            var yPosition = 50f
            while (yPosition + lineHeight < pageHeight && textIndex < textLines.size) {
                canvas.drawText(textLines[textIndex], 50f, yPosition, printTextPaint)
                yPosition += lineHeight
                textIndex++
            }

            pdfDocument!!.finishPage(page)
            pageNumber++
        }

        try {
            pdfDocument!!.writeTo(FileOutputStream(destination.fileDescriptor))
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            pdfDocument!!.close()
            pdfDocument = null
        }

        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }
}