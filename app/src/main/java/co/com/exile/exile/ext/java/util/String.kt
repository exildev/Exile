package co.com.exile.exile.ext.java.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val DATE_PARSER = SimpleDateFormat("MM-dd-yyyy HH:mm a", Locale.getDefault())
val DATE_PARSER2 = SimpleDateFormat("MM/dd/yyyy HH:mm a", Locale.getDefault())

fun String.toChatDate(): Date? {
    return try {
        DATE_PARSER.parse(this)
    } catch (e: Exception) {
        DATE_PARSER2.parse(this)
    }
}