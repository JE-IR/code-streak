package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.User
import com.example.util.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CodeStreak", appName)
  }

  @Test
  fun `level calculation computes correctly`() {
    val level1 = StatsCalculator.calculateUserLevel(120) // 2 hours
    assertEquals(1, level1.level)
    assertEquals("Novice Coder", level1.levelTitle)

    val level2 = StatsCalculator.calculateUserLevel(600) // 10 hours
    assertEquals(2, level2.level)
    assertEquals("Script Explorer", level2.levelTitle)
  }

  @Test
  fun `streak calculation updates properly`() {
    val user = User(
      username = "Dev",
      email = "dev@test.com",
      passwordHash = "1234",
      currentStreak = 3,
      longestStreak = 5,
      lastSessionDateMillis = System.currentTimeMillis() - (12 * 3600 * 1000)
    )
    val (streak, longest) = StatsCalculator.calculateUpdatedStreak(user, System.currentTimeMillis())
    assertEquals(3, streak) // Same day session keeps streak
  }
}

