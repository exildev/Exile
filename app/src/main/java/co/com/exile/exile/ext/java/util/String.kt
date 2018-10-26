package co.com.exile.exile.ext.java.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val DATE_PARSER = SimpleDateFormat("MM-dd-yyyy HH:mm a", Locale.getDefault())

fun String.toChatDate(): Date? {
    return DATE_PARSER.parse(this)
}