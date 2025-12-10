package com.example.habittracker.habits

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Zarządza nawykami - dodawanie, usuwanie, wybór aktywnego
 */
class HabitManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("habit_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS_LIST = "habits_list"
        private const val KEY_ACTIVE_HABIT_ID = "active_habit_id"
        private const val KEY_FIRST_RUN = "first_run"
    }

    // === ZARZĄDZANIE LISTĄ NAWYKÓW ===

    /**
     * Pobiera wszystkie nawyki
     */
    fun getAllHabits(): List<Habit> {
        val habitsJson = prefs.getString(KEY_HABITS_LIST, null)
        return if (habitsJson != null) {
            try {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson(habitsJson, type) ?: emptyList()
            } catch (e: Exception) {
                // Jeśli błąd parsowania, zwróć pustą listę
                emptyList()
            }
        } else {
            // Pierwszego uruchomienia - utwórz domyślny nawyk
            if (isFirstRun()) {
                val defaultHabit = Habit.createDefault()
                saveHabits(listOf(defaultHabit))
                setActiveHabitId(defaultHabit.id)
                setFirstRunCompleted()
                listOf(defaultHabit)
            } else {
                emptyList()
            }
        }
    }

    /**
     * Pobiera aktywne nawyki (isActive = true)
     */
    fun getActiveHabits(): List<Habit> {
        return getAllHabits().filter { it.isActive }
    }

    /**
     * Zapisuje listę nawyków
     */
    private fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS_LIST, habitsJson).apply()
    }

    /**
     * Dodaje nowy nawyk
     */
    fun addHabit(habit: Habit): Boolean {
        return if (habit.isValid()) {
            val currentHabits = getAllHabits().toMutableList()
            currentHabits.add(habit)
            saveHabits(currentHabits)

            // Jeśli to pierwszy nawyk, ustaw go jako aktywny
            if (getActiveHabitId().isEmpty()) {
                setActiveHabitId(habit.id)
            }

            true
        } else {
            false
        }
    }

    /**
     * Aktualizuje istniejący nawyk
     */
    fun updateHabit(updatedHabit: Habit): Boolean {
        return if (updatedHabit.isValid()) {
            val currentHabits = getAllHabits().toMutableList()
            val index = currentHabits.indexOfFirst { it.id == updatedHabit.id }

            if (index != -1) {
                currentHabits[index] = updatedHabit
                saveHabits(currentHabits)
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * Usuwa nawyk (soft delete - ustawia isActive = false)
     */
    fun deactivateHabit(habitId: String): Boolean {
        val currentHabits = getAllHabits().toMutableList()
        val index = currentHabits.indexOfFirst { it.id == habitId }

        return if (index != -1) {
            currentHabits[index] = currentHabits[index].copy(isActive = false)
            saveHabits(currentHabits)

            // Jeśli usuwamy aktywny nawyk, wybierz inny
            if (getActiveHabitId() == habitId) {
                val activeHabits = getActiveHabits()
                if (activeHabits.isNotEmpty()) {
                    setActiveHabitId(activeHabits.first().id)
                } else {
                    setActiveHabitId("")
                }
            }

            true
        } else {
            false
        }
    }

    /**
     * Usuwa nawyk całkowicie (wraz z danymi)
     */
    fun deleteHabitCompletely(habitId: String, dataManager: com.example.habittracker.data.DataManager): Boolean {
        // Usuń dane nawyku
        dataManager.clearHabitData(habitId)

        // Usuń z listy
        val currentHabits = getAllHabits().toMutableList()
        val removed = currentHabits.removeAll { it.id == habitId }

        if (removed) {
            saveHabits(currentHabits)

            // Jeśli usuwamy aktywny nawyk, wybierz inny
            if (getActiveHabitId() == habitId) {
                val activeHabits = getActiveHabits()
                if (activeHabits.isNotEmpty()) {
                    setActiveHabitId(activeHabits.first().id)
                } else {
                    setActiveHabitId("")
                }
            }
        }

        return removed
    }

    // === AKTYWNY NAWYK ===

    /**
     * Pobiera ID aktywnego nawyku
     */
    fun getActiveHabitId(): String {
        return prefs.getString(KEY_ACTIVE_HABIT_ID, "") ?: ""
    }

    /**
     * Ustawia aktywny nawyk
     */
    fun setActiveHabitId(habitId: String) {
        prefs.edit().putString(KEY_ACTIVE_HABIT_ID, habitId).apply()
    }

    /**
     * Pobiera aktywny nawyk
     */
    fun getActiveHabit(): Habit? {
        val activeId = getActiveHabitId()
        return if (activeId.isNotEmpty()) {
            getAllHabits().find { it.id == activeId && it.isActive }
        } else {
            // Jeśli brak aktywnego, wybierz pierwszy dostępny
            val activeHabits = getActiveHabits()
            if (activeHabits.isNotEmpty()) {
                setActiveHabitId(activeHabits.first().id)
                activeHabits.first()
            } else {
                null
            }
        }
    }

    // === WYSZUKIWANIE I SORTOWANIE ===

    /**
     * Wyszukuje nawyki po nazwie
     */
    fun searchHabits(query: String): List<Habit> {
        val searchQuery = query.lowercase().trim()
        return getActiveHabits().filter {
            it.name.lowercase().contains(searchQuery)
        }
    }

    /**
     * Sortuje nawyki
     */
    fun sortHabits(habits: List<Habit>, sortType: HabitSortType): List<Habit> {
        return when (sortType) {
            HabitSortType.NAME_ASC -> habits.sortedBy { it.name.lowercase() }
            HabitSortType.NAME_DESC -> habits.sortedByDescending { it.name.lowercase() }
            HabitSortType.CREATED_DATE_ASC -> habits.sortedBy { it.createdDate }
            HabitSortType.CREATED_DATE_DESC -> habits.sortedByDescending { it.createdDate }
            HabitSortType.MOST_ACTIVE -> {
                // To wymagałoby dostępu do DataManager, na razie sortuj po dacie
                habits.sortedByDescending { it.createdDate }
            }
        }
    }

    // === WALIDACJA I NARZĘDZIA ===

    /**
     * Sprawdza czy nazwa nawyku jest już zajęta
     */
    fun isNameTaken(name: String, excludeId: String = ""): Boolean {
        return getAllHabits().any {
            it.name.equals(name, ignoreCase = true) && it.id != excludeId
        }
    }

    /**
     * Generuje unikatową nazwę (jeśli nazwa już istnieje)
     */
    fun generateUniqueName(baseName: String): String {
        var counter = 1
        var newName = baseName

        while (isNameTaken(newName)) {
            newName = "$baseName ($counter)"
            counter++
        }

        return newName
    }

    /**
     * Pobiera statystyki nawyku
     */
    fun getHabitStats(habitId: String, dataManager: com.example.habittracker.data.DataManager): HabitStats {
        val allStatuses = dataManager.getAllStatuses(habitId)

        var totalDays = 0
        var successDays = 0
        var failureDays = 0

        allStatuses.values.forEach { status ->
            when (status) {
                true -> {
                    successDays++
                    totalDays++
                }
                false -> {
                    failureDays++
                    totalDays++
                }
                null -> {
                    // Nie oznaczono - nie liczy się
                }
            }
        }

        val successRate = if (totalDays > 0) {
            (successDays.toFloat() / totalDays * 100).toInt()
        } else 0

        val currentStreak = dataManager.calculateStreak(habitId)

        return HabitStats(
            habitId = habitId,
            totalMarkedDays = totalDays,
            successDays = successDays,
            failureDays = failureDays,
            successRate = successRate,
            currentStreak = currentStreak
        )
    }

    // === PIERWSZE URUCHOMIENIE ===

    private fun isFirstRun(): Boolean {
        return !prefs.getBoolean(KEY_FIRST_RUN, false)
    }

    private fun setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
    }

    // === ZARZĄDZANIE DANYMI ===

    /**
     * Eksportuje wszystkie nawyki do JSON
     */
    fun exportHabits(): String {
        return gson.toJson(getAllHabits())
    }

    /**
     * Importuje nawyki z JSON
     */
    fun importHabits(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<Habit>>() {}.type
            val importedHabits: List<Habit> = gson.fromJson(json, type)

            // Waliduj zaimportowane nawyki
            val validHabits = importedHabits.filter { it.isValid() }

            if (validHabits.isNotEmpty()) {
                val currentHabits = getAllHabits().toMutableList()

                // Dodaj nowe nawyki (unikając duplikatów po nazwie)
                validHabits.forEach { importedHabit ->
                    if (!isNameTaken(importedHabit.name)) {
                        currentHabits.add(importedHabit)
                    }
                }

                saveHabits(currentHabits)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Czyści wszystkie nawyki
     */
    fun clearAllHabits() {
        prefs.edit()
            .remove(KEY_HABITS_LIST)
            .remove(KEY_ACTIVE_HABIT_ID)
            .apply()
    }
}

/**
 * Statystyki nawyku
 */
data class HabitStats(
    val habitId: String,
    val totalMarkedDays: Int,
    val successDays: Int,
    val failureDays: Int,
    val successRate: Int,        // procent sukcesów
    val currentStreak: Int       // aktualna passa
)