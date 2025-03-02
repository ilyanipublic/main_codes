package com.airprint.smart.printer.utils

import android.print.PrintJob
import android.print.PrintJobInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CustomPrintingMonitorAdapter() {

    private val _printStatusFlow = MutableSharedFlow<PrintStatus>()
    val printStatusFlow: SharedFlow<PrintStatus> = _printStatusFlow.asSharedFlow()

    fun monitorPrintJobStatus(printJob: PrintJob) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1000) // Check every second
                when (printJob.info.state) {
                    PrintJobInfo.STATE_COMPLETED -> {
                        _printStatusFlow.emit(PrintStatus.COMPLETED)
                        break
                    }
                    PrintJobInfo.STATE_FAILED -> {
                        _printStatusFlow.emit(PrintStatus.FAILED)
                        break
                    }
                    PrintJobInfo.STATE_CANCELED -> {
                        _printStatusFlow.emit(PrintStatus.CANCELED)
                        break
                    }
                    PrintJobInfo.STATE_STARTED -> {
                        _printStatusFlow.emit(PrintStatus.IN_PROGRESS)
                    }
                    else -> {
                        _printStatusFlow.emit(PrintStatus.IN_PROGRESS)
                    }
                }
            }
        }
    }
}