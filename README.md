# 📊 Habit Tracker - Android Application

Native Android application for tracking daily habits and building productive routines. Built with modern Android development practices using Kotlin and Jetpack components.

## ✨ Features

- ✅ **Create Custom Habits** - Define your own habits with customizable names and descriptions
- 📅 **Daily Tracking** - Mark habits as complete for each day
- 🔥 **Streak Monitoring** - Track consecutive days to maintain motivation
- 📊 **Progress Visualization** - View your habit completion history
- 💾 **Local Data Persistence** - All data stored locally using Room Database
- 🎨 **Material Design** - Clean, modern UI following Material Design guidelines
- 🌙 **Intuitive UX** - Simple and user-friendly interface

## 🛠️ Tech Stack

- **Language:** Kotlin
- **IDE:** Android Studio
- **Architecture:** MVVM (Model-View-ViewModel) pattern
- **Database:** Room (SQLite)
- **UI Components:** Material Design Components
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## 🏗️ Architecture

The app follows the MVVM architecture pattern to ensure:
- Clear separation of concerns
- Easier testing and maintenance
- Reactive UI updates with LiveData/StateFlow
- Lifecycle-aware components

```
app/
├── data/           # Database entities, DAOs, and repositories
├── ui/             # Activities, Fragments, and ViewModels
├── model/          # Data models
└── util/           # Utility classes and helpers
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Kotlin 1.9+
- Android SDK 24+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/adak101/habit-tracker.git
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## 📦 Key Dependencies

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.0")
kapt("androidx.room:room-compiler:2.6.0")
implementation("androidx.room:room-ktx:2.6.0")

// Lifecycle components
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

// Material Design
implementation("com.google.android.material:material:1.10.0")
```

## 🎯 Core Functionality

### Habit Management
- Add new habits with custom names
- Edit existing habits
- Delete habits with confirmation
- View all habits in a list

### Progress Tracking
- Mark habits as complete/incomplete for each day
- View historical completion data
- Calculate and display current streaks
- Track overall completion statistics

### Data Persistence
- All data stored locally using Room Database
- Automatic backup on device
- No internet connection required
- Fast and efficient data operations

## 🔒 Privacy

All data is stored locally on your device. No personal information is collected or transmitted to any server.

## 📝 Development Principles

- **Clean Code:** Following Kotlin coding conventions
- **Single Responsibility:** Each class has one clear purpose
- **DRY Principle:** Avoid code duplication
- **Type Safety:** Leveraging Kotlin's strong type system
- **Null Safety:** Using Kotlin's null-safety features

## 🧪 Testing

The project includes:
- Unit tests for ViewModels and business logic
- Instrumented tests for database operations
- UI tests for critical user flows

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 👨‍💻 Author

**Adam Waśko**
- GitHub: [@adak101](https://github.com/adak101)
- LinkedIn: [Adam Waśko](https://www.linkedin.com/in/adam-wa%C5%9Bko-b736ba311/)
- Email: adam.wasko48@gmail.com

## 📄 License

This project is open source and available for educational purposes.

## 🙏 Acknowledgments

- Built as a personal project to explore Android development with Kotlin
- Inspired by habit tracking methodologies and behavior psychology
- Implements modern Android best practices and Material Design guidelines

---

⭐ If you find this project useful, please consider giving it a star!
