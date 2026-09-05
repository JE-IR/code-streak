package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatters {

    fun formatDurationHhMmSs(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    fun formatMinutesToHoursAndMinutes(minutes: Long): String {
        val hrs = minutes / 60
        val remainingMins = minutes % 60
        return if (hrs > 0) {
            "${hrs}h ${remainingMins}m"
        } else {
            "${remainingMins}m"
        }
    }

    fun formatSecondsToHoursAndMinutes(seconds: Long): String {
        val totalMinutes = seconds / 60
        return formatMinutesToHoursAndMinutes(totalMinutes)
    }

    fun formatDateTime(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formatDateOnly(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formatTimeOnly(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }
}
