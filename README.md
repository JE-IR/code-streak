# CodeStreak 🔥

> **Ignite your developer habit daily.** CodeStreak is a modern Android application built with Kotlin and Jetpack Compose that makes daily coding practice measurable, rewarding, and habit-forming.

---

## ✨ Key Features

- **Developer Charcoal & Amber Flame Bento Grid Theme**: High-contrast, dark slate/charcoal surfaces with glowing ember accents (`#FF6E14` & `#FFB703`) and vibrant developer category chips.
- **Glassmorphism Authentication**: Sleek frosted-glass sign-in & sign-up forms with ambient glow backdrops and instant **Quick Demo / Developer Mode**.
- **Cinematic Celebration Splash Screen**: Animated developer coding illustration with keyboard typing, pulse flame bursts, and celebratory confetti particles.
- **Active Practice Session & Accelerometer Shake-to-Log**: Start real-time practice sessions; shake your phone or trigger milestones to log problem completions and Pomodoros.
- **Habit & Streak Tracking**:
  - Daily & weekly goal progress rings with real-time target tracking
  - Current streak and longest streak counters
  - 30-Day Activity Heatmap grid with intensity color mapping
  - Future Streak Milestone Projections (7-day, 30-day, 100-day targets)
- **Developer Level Progression**: Earn XP and level up from *Script Novice* to *Algorithm Master* and *Code Grandmaster*.
- **Offline-First Room Database**: Complete local persistence with SQLite Room storing sessions, logs, goals, and user profiles.
- **Developer Wisdom & Quotes**: Daily motivational quotes for software engineers.
- **Social Streak Sharing**: One-tap native Android sharing for milestones and streaks.

---

## 🚀 How to Run and Test the App

### Option 1: Run in Android Studio on your local machine
1. **Clone the project in GitHub**;
2. **Open the project in Android Studio**:
   - Open Android Studio (Ladybug, Meerkat, or newer recommended).
   - Select **Open** and choose the extracted `CodeStreak` folder.
3. **Gradle Sync**: Let Android Studio sync dependencies automatically.
4. **Select Device / Emulator**:
   - Choose a physical Android device (via USB Debugging) or start an Android Virtual Device (AVD with API 26+).
5. **Run the App**: Click the green **Run (▶)** button or press `Shift + F10`.

---

## 🧪 Testing Shake Gesture on Local Android Studio Emulator
To test the accelerometer shake gesture in the local Android Studio emulator:
1. Open the Emulator **Extended Controls** (`...` icon on the emulator sidebar).
2. Select the **Virtual Sensors** tab.
3. Move the **Device Pose / Rotation** sliders vigorously back and forth to simulate shaking, or use the **Accelerometer** input sliders.
4. Alternatively, use the in-app **"Simulate Shake"** button during an active practice session to test milestone logging instantly!

---

## 🏗️ Tech Stack & Architecture

## Tech Stack
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Layering
- **Database**: Android Jetpack Room (SQLite)
- **Sensors**: Android Hardware SensorManager (`Sensor.TYPE_ACCELEROMETER`)
- **Async & Reactive**: Kotlin Coroutines & `StateFlow`


## 🏛️ Architecture


```text
com.example/
├── data/               # Data layer (Entities, DAOs, Room DB, Repositories, Sensors)
├── viewmodel/          # State holders (UI States, Coroutine dispatchers, Business logic)
├── ui/                 # Presentation layer (Jetpack Compose Activities, Components, Theme)
└── util/               # Helper utilities (Date/Time formatting, Math, XP calculations)

```
