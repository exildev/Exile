package co.com.exile.exile.ext.java.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Date.daysDifference(date: Date) = TimeUnit.MILLISECONDS.toDays(time - date.time)

val HOUR_FORMATTER = SimpleDateFormat("HH:mm", Locale.getDefault())
val DATE_FORMATTER = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

fun Date.toChatDateFormat(): String {
    val currentTime = Calendar.getInstance()
    val lookupTime = Calendar.getInstance()
    lookupTime.time = this
    return when (daysDifference(currentTime.time)) {
        0L -> HOUR_FORMATTER.format(this)
        1L -> "Ayer"
        else -> DATE_FORMATTER.format(this)
    }
}