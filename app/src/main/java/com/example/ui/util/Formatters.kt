package com.example.ui.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {
    fun formatViews(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val hours = minutes / 60
        return if (hours > 0) {
            val remMinutes = minutes % 60
            String.format(Locale.US, "%d:%02d:%02d", hours, remMinutes, remainingSeconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, remainingSeconds)
        }
    }

    fun formatTimeAgo(timestamp: Long): String {
        val diffMillis = System.currentTimeMillis() - timestamp
        if (diffMillis < 0) return "Just now"
        
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        
        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    }

    fun formatSubscribers(count: Long): String {
        return "${formatViews(count)} subscribers"
    }
}
