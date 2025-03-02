package com.airprint.smart.printer.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.FileOutputStream

class PdfDocumentAdapter(private val context: Context, private val pdfUri: Uri) : PrintDocumentAdapter() {
    override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback, extras: Bundle) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val builder = PrintDocumentInfo.Builder("document.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback.onLayoutFinished(builder, true)
    }

    override fun onWrite(pages: Array<PageRange>?, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal?, callback: WriteResultCallback) {
        val input = context.contentResolver.openInputStream(pdfUri)
        val output = FileOutputStream(destination.fileDescriptor)
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (input?.read(buf).also { bytesRead = it ?: 0 } != -1) {
            output.write(buf, 0, bytesRead)
        }
        input?.close()
        output.close()
        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }
}