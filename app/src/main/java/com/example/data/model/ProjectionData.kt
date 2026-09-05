package com.example.data.model

data class ProjectionData(
    val dailyPaceMinutes: Float,
    val weeklyPaceHours: Float,
    val hoursTo50Goal: Float,
    val weeksTo50Goal: Float,
    val hoursTo100Goal: Float,
    val weeksTo100Goal: Float,
    val hoursTo1000Goal: Float,
    val monthsTo1000Goal: Float,
    val totalHoursLogged: Float,
    val projectedLevelIn30Days: Int
)
