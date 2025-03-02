package com.airprint.smart.printer.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import androidx.viewbinding.ViewBinding

class CustomDialog<T : ViewBinding>(
    private val context: Context,
    private val binding: T,
    private val gravity: Int = Gravity.CENTER,
    private val widthMarginDp: Int = 15,
    private val bottomMarginDp: Int = 12,
    private val isCancelable: Boolean = false
) {
    private val dialog = Dialog(context)

    init {
        setupDialog()
    }

    private fun setupDialog() {
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = gravity

        // Set width and bottom margin in pixels based on the provided DP
        val widthMarginPx = context.resources.displayMetrics.density * widthMarginDp
        val bottomMarginPx = context.resources.displayMetrics.density * bottomMarginDp

        layoutParams?.width = (binding.root.width - widthMarginPx).toInt()
        layoutParams?.y = bottomMarginPx.toInt()

        window?.attributes = layoutParams
        dialog.setCancelable(isCancelable)
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}