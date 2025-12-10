package com.example.habittracker

import android.Manifest
import android.app.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.habittracker.calendar.CalendarView
import com.example.habittracker.data.DataManager
import com.example.habittracker.habits.*
import com.example.habittracker.notifications.NotificationScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var dataManager: DataManager
    private lateinit var habitManager: HabitManager
    private lateinit var streakText: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var habitSelector: HabitSelector

    private var currentHabit: Habit? = null

    // Rejestracja uprawnień na powiadomienia
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Brak zgody na powiadomienia – przypomnienie nie zostanie ustawione.", Toast.LENGTH_LONG).show()
            } else {
                // Spróbuj ponownie ustawić przypomnienie jeśli uprawnienie uzyskano
                currentHabit?.let { habit ->
                    if (habit.reminderHour != null && habit.reminderMinute != null) {
                        NotificationScheduler.scheduleDailyReminder(
                            this,
                            habit.id,
                            habit.reminderHour!!,
                            habit.reminderMinute!!,
                            habit.name
                        )
                        Toast.makeText(this, "Przypomnienie zostało ustawione.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataManager = DataManager(this)
        habitManager = HabitManager(this)

        createLayout()
        setupHabitSelector()
        updateUI()
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun askNotificationPermissionIfNeeded(onGranted: () -> Unit) {
        if (hasNotificationPermission()) {
            onGranted()
        } else {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        habitSelector = HabitSelector(this).apply {
            setPadding(0, 0, 0, 16)
        }

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val successButton = Button(this).apply {
            text = "✓ Sukces"
            setOnClickListener {
                markTodayAs(true)
            }
        }

        val failButton = Button(this).apply {
            text = "✗ Porażka"
            setOnClickListener {
                markTodayAs(false)
            }
        }

        streakText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        calendarView = CalendarView(this).apply {
            setOnDayClickListener { date ->
                showDayDialog(date)
            }
        }

        val hintText = TextView(this).apply {
            text = "👆 Kliknij dowolny dzień aby go oznaczyć"
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
        }

        val managementLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        val statsButton = Button(this).apply {
            text = "Statystyki"
            setOnClickListener {
                showStatsDialog()
            }
        }

        val resetButton = Button(this).apply {
            text = "Opcje"
            setOnClickListener {
                showResetDialog()
            }
        }

        buttonLayout.addView(successButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        buttonLayout.addView(failButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        managementLayout.addView(statsButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        managementLayout.addView(resetButton, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        mainLayout.addView(habitSelector)
        mainLayout.addView(buttonLayout)
        mainLayout.addView(streakText)
        mainLayout.addView(calendarView)
        mainLayout.addView(hintText)
        mainLayout.addView(managementLayout)

        setContentView(mainLayout)
    }

    private fun setupHabitSelector() {
        habitSelector.initialize(habitManager)

        habitSelector.setOnHabitSelectedListener { habit ->
            currentHabit = habit
            updateUI()
        }

        habitSelector.setOnAddHabitClickListener {
            showCreateHabitDialog()
        }
    }

    private fun updateUI() {
        if (currentHabit == null) {
            currentHabit = habitManager.getActiveHabit()
        }

        if (currentHabit != null) {
            val habitId = currentHabit!!.id

            val streak = dataManager.calculateStreak(habitId)
            val fireIcon = if (streak >= 2) "🔥 " else ""
            streakText.text = "${fireIcon}Passa: $streak dni"

            updateCalendar()
        } else {
            streakText.text = "Passa: 0 dni"
            calendarView.updateDayStatuses(emptyMap())
        }
    }

    private fun updateCalendar() {
        if (currentHabit != null) {
            val statuses = dataManager.getAllStatuses(currentHabit!!.id)
            calendarView.updateDayStatuses(statuses)
        }
    }

    private fun markTodayAs(success: Boolean) {
        if (currentHabit == null) {
            Toast.makeText(this, "Najpierw dodaj nawyk", Toast.LENGTH_SHORT).show()
            return
        }

        val today = dataManager.getTodayDateString()
        dataManager.saveDayStatus(currentHabit!!.id, today, success)
        updateUI()

        val message = if (success) "Oznaczono jako sukces" else "Oznaczono jako porażka"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDayDialog(date: String) {
        if (currentHabit == null) {
            Toast.makeText(this, "Najpierw wybierz nawyk", Toast.LENGTH_SHORT).show()
            return
        }

        val habitId = currentHabit!!.id
        val currentStatus = dataManager.getDayStatus(habitId, date)
        val displayDate = dataManager.formatDateForDisplay(date)

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 8)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val messageView = TextView(this).apply {
            text = "${currentHabit!!.icon} ${currentHabit!!.name}\n$displayDate\n\nAktualny status: ${
                when (currentStatus) {
                    true -> "✅ Sukces"
                    false -> "❌ Porażka"
                    null -> "⚪ Nie oznaczono"
                }
            }"
            setPadding(0, 0, 0, 24)
            textSize = 16f
            gravity = Gravity.CENTER_HORIZONTAL
        }
        dialogLayout.addView(messageView)

        var dialog: AlertDialog? = null

        val btnSuccess = Button(this).apply {
            text = "✓ Sukces"
            setOnClickListener {
                dataManager.saveDayStatus(habitId, date, true)
                updateUI()
                Toast.makeText(this@MainActivity, "✓ Oznaczono jako sukces", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }
        val btnFail = Button(this).apply {
            text = "✗ Porażka"
            setOnClickListener {
                dataManager.saveDayStatus(habitId, date, false)
                updateUI()
                Toast.makeText(this@MainActivity, "✗ Oznaczono jako porażka", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }
        val btnRemove = Button(this).apply {
            text = "⚪ Usuń oznaczenie"
            setOnClickListener {
                dataManager.removeDayStatus(habitId, date)
                updateUI()
                Toast.makeText(this@MainActivity, "⚪ Usunięto oznaczenie", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }

        dialogLayout.addView(btnSuccess)
        dialogLayout.addView(btnFail)
        dialogLayout.addView(btnRemove)

        dialog = AlertDialog.Builder(this)
            .setTitle("Edytuj dzień")
            .setView(dialogLayout)
            .setNegativeButton("Anuluj", null)
            .create()
        dialog.show()
    }

    // ---- STATYSTYKI, RESET, USUWANIE, USTAWIANIE PRZYPOMNIENIA ----
    private fun showStatsDialog() {
        if (currentHabit == null) {
            Toast.makeText(this, "Najpierw wybierz nawyk", Toast.LENGTH_SHORT).show()
            return
        }

        val stats = habitManager.getHabitStats(currentHabit!!.id, dataManager)
        val message = """
            Statystyki: ${currentHabit!!.name}

            Oznaczonych dni: ${stats.totalMarkedDays}
            Sukcesy: ${stats.successDays}
            Porażki: ${stats.failureDays}
            Procent sukcesu: ${stats.successRate}%
            Aktualna passa: ${stats.currentStreak} dni
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Statystyki")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showResetDialog() {
        val options = arrayOf(
            "Ustaw przypomnienie",
            "Wyczyść dane tego nawyku",
            "Wyczyść wszystkie dane",
            "Usuń ten nawyk całkowicie"
        )

        AlertDialog.Builder(this)
            .setTitle("Opcje")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showReminderTimePicker()
                    1 -> showConfirmResetHabitDialog()
                    2 -> showConfirmResetAllDialog()
                    3 -> showConfirmDeleteHabitDialog()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showConfirmResetHabitDialog() {
        if (currentHabit == null) return

        AlertDialog.Builder(this)
            .setTitle("Wyczyść dane nawyku")
            .setMessage("Czy na pewno chcesz usunąć wszystkie dane dla nawyku \"${currentHabit!!.name}\"? Tej operacji nie można cofnąć.")
            .setPositiveButton("Wyczyść") { _, _ ->
                dataManager.clearHabitData(currentHabit!!.id)
                updateUI()
                Toast.makeText(this, "Dane nawyku zostały wyczyszczone", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showConfirmResetAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Wyczyść wszystkie dane")
            .setMessage("Czy na pewno chcesz usunąć WSZYSTKIE dane i nawyki? Tej operacji nie można cofnąć.")
            .setPositiveButton("Wyczyść") { _, _ ->
                dataManager.clearAllData()
                habitManager.clearAllHabits()
                currentHabit = null
                habitSelector.refreshHabits()
                updateUI()
                Toast.makeText(this, "Wszystkie dane zostały usunięte", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showConfirmDeleteHabitDialog() {
        if (currentHabit == null) return

        AlertDialog.Builder(this)
            .setTitle("Usuń nawyk")
            .setMessage("Czy na pewno chcesz usunąć nawyk \"${currentHabit!!.name}\" wraz ze wszystkimi danymi? Tej operacji nie można cofnąć.")
            .setPositiveButton("Usuń") { _, _ ->
                habitManager.deleteHabitCompletely(currentHabit!!.id, dataManager)
                currentHabit = habitManager.getActiveHabit()
                habitSelector.refreshHabits()
                updateUI()
                Toast.makeText(this, "Nawyk został usunięty", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    /*** POWIADOMIENIA – Wybór godziny i ustawienie alarmu ***/
    private fun showReminderTimePicker() {
        val habit = currentHabit ?: run {
            Toast.makeText(this, "Najpierw wybierz nawyk", Toast.LENGTH_SHORT).show()
            return
        }
        val currentHour = habit.reminderHour ?: 20
        val currentMinute = habit.reminderMinute ?: 0

        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val updatedHabit = habit.copy(reminderHour = hourOfDay, reminderMinute = minute)
                habitManager.updateHabit(updatedHabit)
                currentHabit = updatedHabit
                askNotificationPermissionIfNeeded {
                    NotificationScheduler.scheduleDailyReminder(
                        this,
                        updatedHabit.id,
                        hourOfDay,
                        minute,
                        updatedHabit.name
                    )
                    Toast.makeText(
                        this,
                        "Ustawiono przypomnienie: $hourOfDay:${String.format("%02d", minute)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            currentHour,
            currentMinute,
            true
        )
        timePicker.setTitle("Wybierz godzinę przypomnienia")
        timePicker.show()
    }

    private fun showCreateHabitDialog() {
        val dialog = CreateHabitDialog(this, habitManager)
        dialog.setOnHabitCreatedListener { habit ->
            currentHabit = habit
            habitSelector.refreshHabits()
            habitSelector.selectHabit(habit.id)
            updateUI()
            // Ustaw przypomnienie (jeśli wybrano godzinę)
            if (habit.reminderHour != null && habit.reminderMinute != null) {
                askNotificationPermissionIfNeeded {
                    NotificationScheduler.scheduleDailyReminder(
                        this,
                        habit.id,
                        habit.reminderHour!!,
                        habit.reminderMinute!!,
                        habit.name
                    )
                }
            }
        }
        dialog.show()
    }
}
