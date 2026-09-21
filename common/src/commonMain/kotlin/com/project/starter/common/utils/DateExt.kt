package com.project.starter.common.utils

// Simple date formatter placeholder since java.text.SimpleDateFormat is not available in KMP commonMain
// Consider adding kotlinx-datetime for robust KMP date handling
fun Long.toFormattedDateString(): String {
    return this.toString()
}
