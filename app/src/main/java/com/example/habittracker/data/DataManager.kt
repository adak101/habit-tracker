package com.example.habittracker.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class DataManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val DEFAULT_HABIT_ID = "default_habit"
    }

    // === PODSTAWOWE OPERACJE NA DANYCH ===

    /**
     * Zapisuje status dnia dla domyślnego nawyku
     */
    fun saveDayStatus(date: String, success: Boolean) {
        saveDayStatus(DEFAULT_HABIT_ID, date, success)
    }

    /**
     * Zapisuje status dnia dla konkretnego nawyku
     */
    fun saveDayStatus(habitId: String, date: String, success: Boolean) {
        val key = "${habitId}_${date}_success"
        prefs.edit().putBoolean(key, success).apply()
    }

    /**
     * Pobiera status dnia dla domyślnego nawyku
     */
    fun getDayStatus(date: String): Boolean? {
        return getDayStatus(DEFAULT_HABIT_ID, date)
    }

    /**
     * Pobiera status dnia dla konkretnego nawyku
     */
    fun getDayStatus(habitId: String, date: String): Boolean? {
        val key = "${habitId}_${date}_success"
        return when {
            prefs.contains(key) -> prefs.getBoolean(key, false)
            else -> null
        }
    }

    /**
     * Usuwa status dnia dla domyślnego nawyku
     */
    fun removeDayStatus(date: String) {
        removeDayStatus(DEFAULT_HABIT_ID, date)
    }

    /**
     * Usuwa status dnia dla konkretnego nawyku
     */
    fun removeDayStatus(habitId: String, date: String) {
        val key = "${habitId}_${date}_success"
        prefs.edit().remove(key).apply()
    }

    // === OPERACJE NA MIESIĄCACH ===

    /**
     * Pobiera wszystkie statusy dla domyślnego nawyku
     */
    fun getAllStatuses(): Map<String, Boolean?> {
        return getAllStatuses(DEFAULT_HABIT_ID)
    }

    /**
     * Pobiera wszystkie statusy dla konkretnego nawyku
     */
    fun getAllStatuses(habitId: String): Map<String, Boolean?> {
        val statuses = mutableMapOf<String, Boolean?>()
        val allPrefs = prefs.all
        val prefix = "${habitId}_"
        val suffix = "_success"

        for ((key, value) in allPrefs) {
            if (key.startsWith(prefix) && key.endsWith(suffix)) {
                val date = key.removePrefix(prefix).removeSuffix(suffix)
                statuses[date] = value as? Boolean
            }
        }

        return statuses
    }

    /**
     * Pobiera statusy dla konkretnego miesiąca
     */
    fun getMonthStatuses(year: Int, month: Int): Map<String, Boolean?> {
        return getMonthStatuses(DEFAULT_HABIT_ID, year, month)
    }

    /**
     * Pobiera statusy dla konkretnego miesiąca i nawyku
     */
    fun getMonthStatuses(habitId: String, year: Int, month: Int): Map<String, Boolean?> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val statuses = mutableMapOf<String, Boolean?>()

        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val date = dateFormat.format(calendar.time)
            statuses[date] = getDayStatus(habitId, date)
        }

        return statuses
    }

    // === OBLICZENIA I STATYSTYKI ===

    /**
     * Oblicza aktualną passę dla domyślnego nawyku
     */
    fun calculateStreak(): Int {
        return calculateStreak(DEFAULT_HABIT_ID)
    }

    /**
     * Oblicza aktualną passę dla konkretnego nawyku
     */
    fun calculateStreak(habitId: String): Int {
        val calendar = Calendar.getInstance()
        var streak = 0

        // Sprawdź od dzisiaj wstecz
        for (i in 0 until 365) {
            val date = dateFormat.format(calendar.time)
            val status = getDayStatus(habitId, date)

            when (status) {
                true -> streak++
                false -> break // Porażka przerywa passę
                null -> {
                    // Brak oznaczenia - jeśli to pierwszy dzień sprawdzania, nie liczy się jako passa
                    // Jeśli mamy już jakąś passę, to ją przerywa
                    if (i == 0) {
                        // Dzisiaj nie oznaczono - brak passy
                        break
                    } else {
                        // W środku passy - nie przerywa, ale też nie wydłuża
                        // Kontynuujemy sprawdzanie czy wcześniej była passa
                    }
                }
            }

            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }

    /**
     * Oblicza statystyki dla miesiąca
     */
    fun getMonthStats(year: Int, month: Int): MonthStats {
        return getMonthStats(DEFAULT_HABIT_ID, year, month)
    }

    /**
     * Oblicza statystyki dla miesiąca i nawyku
     */
    fun getMonthStats(habitId: String, year: Int, month: Int): MonthStats {
        val statuses = getMonthStatuses(habitId, year, month)

        var successDays = 0
        var failureDays = 0
        var totalMarkedDays = 0

        statuses.values.forEach { status ->
            when (status) {
                true -> {
                    successDays++
                    totalMarkedDays++
                }
                false -> {
                    failureDays++
                    totalMarkedDays++
                }
                null -> {
                    // Nie oznaczono - nie liczy się
                }
            }
        }

        val totalDays = statuses.size
        val successRate = if (totalMarkedDays > 0) {
            (successDays.toFloat() / totalMarkedDays * 100).toInt()
        } else 0

        return MonthStats(
            year = year,
            month = month,
            totalDays = totalDays,
            successDays = successDays,
            failureDays = failureDays,
            unmarkedDays = totalDays - totalMarkedDays,
            successRate = successRate
        )
    }

    // === ZARZĄDZANIE DANYMI ===

    /**
     * Czyści wszystkie dane
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    /**
     * Czyści dane dla konkretnego nawyku
     */
    fun clearHabitData(habitId: String) {
        val editor = prefs.edit()
        val allPrefs = prefs.all
        val prefix = "${habitId}_"

        for (key in allPrefs.keys) {
            if (key.startsWith(prefix)) {
                editor.remove(key)
            }
        }

        editor.apply()
    }

    // === NARZĘDZIA ===

    /**
     * Pobiera dzisiejszą datę jako string
     */
    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    /**
     * Formatuje datę do wyświetlenia (dd.MM.yyyy)
     */
    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}

/**
 * Statystyki miesiąca
 */
data class MonthStats(
    val year: Int,
    val month: Int,
    val totalDays: Int,
    val successDays: Int,
    val failureDays: Int,
    val unmarkedDays: Int,
    val successRate: Int // procent sukcesów z oznaczonych dni
)