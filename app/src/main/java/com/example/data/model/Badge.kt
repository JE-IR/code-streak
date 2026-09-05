package com.example.data.model

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val requiredValue: Int,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val category: String // "STREAK", "HOURS", "SHAKES", "SESSIONS"
)

data class UserLevelInfo(
    val level: Int,
    val levelTitle: String,
    val currentXp: Long, // in minutes
    val nextLevelXp: Long,
    val progressFraction: Float
)
