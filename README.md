# 🧠 BrainHeal

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09.00-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-M3-7B1FA2?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Room Database](https://img.shields.io/badge/Room%20DB-2.7.0-4CAF50?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Android API](https://img.shields.io/badge/API-24%20..%2036-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Privacy](https://img.shields.io/badge/100%25%20Offline-Zero%20Tracking-00C853?style=for-the-badge&logo=adblockplus&logoColor=white)](#-zero-tracking-guarantee)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

<br/>

**A distraction-free, zero-SaaS, 100% offline-first daily executive function and
focus companion designed specifically for ADHD and neurodivergent minds.**

[Core Philosophy](#-core-philosophy) • [Key Features](#-key-features) •
[Architecture](#-system-architecture) • [Getting Started](#-getting-started) •
[Privacy](#-zero-tracking-guarantee) • [Contributing](#-contributing)

</div>

---

## 💡 Core Philosophy

Traditional productivity apps often trigger **executive dysfunction** and
**cognitive overload** through overwhelming feature bloat, push notifications,
continuous SaaS upsells, and complex hierarchical tag systems.

**BrainHeal (`com.emirozturk.brainheal`)** takes an uncompromising, humane
approach:

1. **Single-Task Spotlight (Anti-Paralysis):** Instead of staring at an endless
   backlog, the interface narrows down to one active micro-step at a time.
2. **Energy-First Scheduling:** Work is categorized not merely by artificial
   deadlines, but by real neurological energy states (_Low Energy 5–10m_,
   _Medium Energy 15–30m_, _High Focus 45m+_).
3. **Sensory Overload Protection:** Clean typography, gentle haptics, optional
   AMOLED pitch-black theming, and a dedicated Ultra-Minimalist Mode.
4. **Impulse Spending Shield:** A built-in 48-hour cooling-off buffer for
   impulse desires to curb dopamine-seeking financial behavior.
5. **Absolute Data Sovereignty:** 100% on-device Room database. No accounts, no
   backend telemetry, no background data harvesting.

---

## ✨ Key Features

### 🎯 1. Single Task Focus & Pomodoro Mode

- **Zero Decision Paralysis:** Highlights one specific task on a
  distraction-free screen (`FocusSingleTaskScreen`).
- **Flexible Pomodoro Timer:** Configurable interval countdowns with quick
  extension triggers (`+5m`, `+10m`).
- **Granular Subtask Chunking:** Break intimidating monolith tasks into
  bite-sized checkboxes.
- **Positive Reinforcement:** Visual celebration animations and confetti bursts
  (`ConfettiCelebrationOverlay`) upon micro-milestone completions.

### 📋 2. ADHD-Friendly Task Management

- **Triaged Priority Matrix:** Simple priority tiers (_Urgent & Important_,
  _Important_, _Casual / Low Stakes_) with distinctive, high-contrast
  indicators.
- **Energy-Level Tagging:** Filter tasks instantly based on current mental
  capacity:
  - 🟢 **Low Energy (5–10 min):** Quick wins to build initial momentum.
  - 🟡 **Medium Energy (15–30 min):** Structured, steady-paced duties.
  - 🔴 **High Focus (45+ min):** Deep-work immersion blocks.
- **Reactive State Flow:** Instant subtask toggling and status transitions
  backed by Room Flow queries.

### 📝 3. Minimalist Brain-Dump Notes

- **Frictionless Capture:** Rapidly dump thoughts, thoughts in flight, or
  sensory reminders without mandatory tagging or sorting steps.
- **Interactive Checklists:** Convert any note into a real-time checklist
  (`NoteChecklistItem`) with single-tap completion.
- **Color Accent Organization:** Distraction-free dark/slate surface palettes
  (`NoteEntity`) with pin-to-top support.

### 📅 4. Low-Stress Timeline & Calendar

- **Sensory-Gentle Agenda:** Clear daily and monthly overviews without stressful
  visual clutter (`CalendarScreen`).
- **All-Day & Timed Blocks:** Track meetings, self-care checkpoints, and
  routines with custom color highlights.

### 💳 5. Expense Tracking & Impulse Guard

- **Native Canvas Pie Chart (`ExpensePieChart`):** Smoothly animated,
  interactive Compose `Canvas` breakdown of spending categories with
  tap-to-inspect feedback.
- **Budget Pacing:** Visual progress against daily and monthly threshold
  budgets.
- **ADHD Impulse Spending Protection:** Built-in 48-hour cooling-off lock for
  non-essential purchase urges (`isImpulseWishlist`, `coolingHours`), preventing
  regretful impulsive shopping.

### 🎛️ 6. Ultra Minimalist Mode

- **Sensory Decompression:** Strips down all secondary tabs, widgets, and
  navigations into a singular, high-contrast, essential action interface
  (`MinimalistModeScreen`).
- **AMOLED Pitch Black Theme:** Minimizes eye fatigue and conserves battery on
  OLED devices.

### 💾 7. Local Data Sovereignty & JSON Portability

- **Complete Offline Privacy:** All data resides exclusively in SQLite via Room
  (`odak_flow_adhd_db`).
- **One-Click JSON Import / Export:** Seamless backup and restoration via
  `BackupJsonExporter` and `BackupJsonImporter` with clipboard and file-sharing
  support.

---

## 🏗️ System Architecture

BrainHeal follows the modern **Unidirectional Data Flow (UDF)** and **MVVM**
architecture, adhering to modern Android Jetpack development best practices.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI LAYER (Compose)                    │
│   TasksScreen  │ FocusSingleTaskScreen │ ExpensesScreen      │
│   NotesScreen  │ CalendarScreen        │ MinimalistScreen   │
│   ExpensePieChart (Custom Canvas) │ SettingsScreen          │
└──────────────────────────────▲──────────────────────────────┘
                               │ UI State (StateFlow)
                               │ User Events (Callbacks)
┌──────────────────────────────┴──────────────────────────────┐
│                    VIEWMODEL LAYER                          │
│                     MainViewModel                           │
│   (StateFlow, CoroutineScope, Timer & Filter State Logic)   │
└──────────────────────────────▲──────────────────────────────┘
                               │ Reactive Kotlin Flows
                               │ Suspend Functions
┌──────────────────────────────┴──────────────────────────────┐
│                   DATA & REPOSITORY LAYER                   │
│                     OdakRepository                          │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │               Room Database Engine                  │   │
│   │             (odak_flow_adhd_db - v2)                │   │
│   │                                                     │   │
│   │  • TaskEntity         -> TaskDao                    │   │
│   │  • NoteEntity         -> NoteDao                    │   │
│   │  • ExpenseEntity      -> ExpenseDao                 │   │
│   │  • CalendarEventEntity-> CalendarEventDao           │   │
│   │  • AppSettingsEntity  -> AppSettingsDao             │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
│   • BackupJsonExporter / BackupJsonImporter (JSON Utils)    │
└─────────────────────────────────────────────────────────────┘
```

### 🛠️ Tech Stack & Dependencies

| Layer / Domain      | Technology                                                                              | Version             | Purpose                                                             |
| :------------------ | :-------------------------------------------------------------------------------------- | :------------------ | :------------------------------------------------------------------ |
| **Language**        | [Kotlin](https://kotlinlang.org/)                                                       | `2.2.10`            | 100% Kotlin codebase with Coroutines & StateFlow                    |
| **UI Toolkit**      | [Jetpack Compose](https://developer.android.com/jetpack/compose)                        | `2024.09.00 (BOM)`  | Declarative UI, Animations, Custom Canvas Drawing                   |
| **Design System**   | [Material Design 3](https://m3.material.io/)                                            | Latest Compose M3   | Adaptive color themes (AMOLED, Dark, Light)                         |
| **Persistence**     | [Room Database](https://developer.android.com/training/data-storage/room)               | `2.7.0`             | Local relational database with Kotlin Coroutines Flow               |
| **Architecture**    | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | `2.8.7`             | `ViewModel`, `lifecycle-runtime-compose`, `StateFlow`               |
| **Navigation**      | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)          | `2.8.9`             | Seamless in-app routing & tab switching                             |
| **Code Generation** | [KSP (Symbol Processing)](https://github.com/google/ksp)                                | `2.3.5`             | High-speed annotation processing for Room & Moshi                   |
| **Build System**    | [Gradle Kotlin DSL](https://gradle.org/)                                                | AGP `9.1.1`         | Type-safe Gradle builds with Version Catalog (`libs.versions.toml`) |
| **Testing**         | [JUnit4](https://junit.org/), [Roborazzi](https://github.com/takahirom/roborazzi)       | `4.13.2` / `1.59.0` | Unit testing & Screenshot regression testing                        |

---

## 🗂️ Project Structure

```text
com.emirozturk.brainheal
├── MainActivity.kt                 # Single Activity entry point & theme coordinator
├── data
│   ├── AppDatabase.kt              # Room Database configuration (odak_flow_adhd_db)
│   ├── dao                         # Data Access Objects (TaskDao, ExpenseDao, NoteDao, etc.)
│   ├── model                       # Immutable data models & entities
│   │   ├── AppSettingsEntity.kt    # Theme, language, budget, and mode settings
│   │   ├── CalendarEventEntity.kt  # Schedule & event entity
│   │   ├── ExpenseEntity.kt        # Expense entity & impulse protection attributes
│   │   ├── NoteEntity.kt           # Notes & checklist structure
│   │   ├── PomodoroMode.kt         # Focus & break cycle configurations
│   │   └── TaskEntity.kt           # Tasks, subtasks, priority & energy levels
│   └── repository
│       └── OdakRepository.kt       # Single source of truth abstracting DAOs
├── ui
│   ├── components                  # Reusable UI widgets (ExpensePieChart, Confetti, etc.)
│   ├── screens
│   │   ├── calendar                # Calendar & Event dialogs
│   │   ├── expenses                # Budget, transaction list & expense dialogs
│   │   ├── minimalist              # High-contrast sensory-safe single-focus view
│   │   ├── notes                   # Brain-dump & checklist screen
│   │   ├── settings                # Preferences, backup, language & theme controls
│   │   └── tasks                   # Task triage list & Single Task Focus screen
│   ├── theme                       # Color schemes, typography, shapes & dynamic theme
│   └── viewmodel
│       └── MainViewModel.kt        # Central state holder & business logic dispatcher
└── util
    ├── BackupJsonExporter.kt       # On-device JSON data serialization
    └── BackupJsonImporter.kt       # Robust validation & JSON deserialization
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK:** Java Development Kit 17+
- **Android Studio:** Ladybug (2024.2.1+) or newer
- **Android SDK:**
  - `compileSdk`: **36**
  - `targetSdk`: **36**
  - `minSdk`: **24** (Android 7.0 Nougat+)

### 📥 Clone & Build

1. Clone the repository:
   ```bash
   git clone https://github.com/emirozturk/BrainHeal.git
   cd BrainHeal
   ```

2. Assemble the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

3. Run unit & Robolectric tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. Install directly to a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 🔒 Zero-Tracking Guarantee

In a world filled with predatory attention economies, **BrainHeal is your
digital sanctuary**:

- 🚫 **No Analytics or Telemetry:** No Google Analytics, no Mixpanel, no
  Facebook SDK, no user behavior fingerprinting.
- 🚫 **No Network Requirement:** The application operates completely offline
  without needing an active internet connection.
- 🚫 **No Accounts or Logins:** You never need to sign in, link an email, or
  submit personal credentials.
- 🛡️ **Your Data Stays on Your Device:** All your tasks, financial numbers,
  personal thoughts, and routines are stored exclusively on your device's
  internal storage and can be backed up as plain JSON anytime.

---

## 🤝 Contributing

Contributions from the open-source community, neurodivergent developers, and
accessibility advocates are warmly welcomed!

1. **Fork the Repository**
2. **Create a Feature Branch:**
   `git checkout -b feature/sensory-friendly-improvement`
3. **Commit Your Changes:**
   `git commit -m "Add custom interval presets to Focus Mode"`
4. **Push to Your Branch:**
   `git push origin feature/sensory-friendly-improvement`
5. **Open a Pull Request**

Please ensure all changes pass existing unit tests and maintain strict offline
privacy.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE)
file for full details.

---

<div align="center">
Made with 💜 for neurodivergent thinkers, builders, and mindful doers.
</div>
