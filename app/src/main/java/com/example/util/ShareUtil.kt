package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ShareUtil {

    fun shareStreak(context: Context, username: String, streakDays: Int, totalHours: Float, levelTitle: String) {
        val message = """
            🔥 CodeStreak Practice Milestone!
            
            👨‍💻 Developer: $username
            ⚡ Current Streak: $streakDays days
            ⏱️ Total Practice Time: ${String.format("%.1f", totalHours)} hours
            🎖️ Rank: $levelTitle
            
            Building consistency one line of code at a time with #CodeStreak!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My CodeStreak Progress!")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooser = Intent.createChooser(intent, "Share CodeStreak Progress")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareSession(context: Context, title: String, category: String, durationStr: String, shakes: Int) {
        val message = """
            ✅ Completed a coding practice session on CodeStreak!
            
            📌 Topic: $title ($category)
            ⏳ Duration: $durationStr
            📳 Milestones Shaken: $shakes
            
            #CodeStreak #100DaysOfCode #DevHabit
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Code Practice Logged!")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooser = Intent.createChooser(intent, "Share Session Summary")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWebUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSupportEmail(context: Context, userEmail: String, username: String) {
        val subject = "CodeStreak Support & Feedback - $username"
        val body = """
            Hi CodeStreak Team,

            Username: $username
            Registered Email: $userEmail
            App Version: 1.0.0
            
            My feedback / inquiry:
            
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@codestreak.dev")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@codestreak.dev"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Contact Support via Email"))
        } catch (e: Exception) {
            Toast.makeText(context, "No email application installed.", Toast.LENGTH_SHORT).show()
        }
    }
}