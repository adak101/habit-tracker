package com.example.habittracker.habits

import java.util.*

/**
 * Model pojedynczego nawyku
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String,
    val color: String,
    val createdDate: String,
    val isActive: Boolean = true,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
) {
    companion object {
        // Domyślny nawyk (dla kompatybilności z istniejącymi danymi)
        const val DEFAULT_HABIT_ID = "default_habit"

        /**
         * Tworzy domyślny nawyk (dla kompatybilności)
         */
        fun createDefault(): Habit {
            return Habit(
                id = DEFAULT_HABIT_ID,
                name = "Mój nawyk",
                icon = "🎯",
                color = "#4CAF50",
                createdDate = getCurrentDateString()
            )
        }

        private fun getCurrentDateString(): String {
            return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    /**
     * Sprawdza czy nawyk ma poprawne dane
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                icon.isNotBlank() &&
                color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    /**
     * Formatuje nazwę do wyświetlenia
     */
    fun getDisplayName(): String {
        return "$icon $name"
    }
}

/**
 * Typ sortowania nawyków
 */
enum class HabitSortType {
    NAME_ASC,           // Alfabetycznie A-Z
    NAME_DESC,          // Alfabetycznie Z-A
    CREATED_DATE_ASC,   // Najstarsze pierwsze
    CREATED_DATE_DESC,  // Najnowsze pierwsze
    MOST_ACTIVE         // Najbardziej aktywne (najwięcej dni oznaczonych)
}