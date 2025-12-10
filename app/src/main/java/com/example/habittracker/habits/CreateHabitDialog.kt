package com.example.habittracker.habits

import android.app.AlertDialog
import android.content.Context
import android.widget.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog do tworzenia nowego nawyku z wyborem ikony
 */
class CreateHabitDialog(
    private val context: Context,
    private val habitManager: HabitManager
) {

    private var onHabitCreatedListener: ((Habit) -> Unit)? = null
    private var selectedIcon: String = "🎯" // domyślna ikona

    // Popularne ikony do wyboru
    private val availableIcons = listOf(
        "🎯", "💪", "📚", "🏃", "💧", "🧘", "🌱", "⏰",
        "🍎", "💤", "✍️", "🎵", "🚶", "🏋️", "🧹", "📱",
        "☕", "🎨", "💰", "🌞"
    )

    /**
     * Pokazuje dialog tworzenia nawyku
     */
    fun show() {
        val dialogLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        // Nazwa nawyku
        val nameLabel = TextView(context).apply {
            text = "Nazwa nawyku:"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }

        val editText = EditText(context).apply {
            hint = "np. Ćwiczenia, Czytanie, Medytacja..."
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setSingleLine(true)
        }

        // Wybór ikony
        val iconLabel = TextView(context).apply {
            text = "Ikona:"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }

        val iconContainer = createIconSelector()

        dialogLayout.addView(nameLabel)
        dialogLayout.addView(editText)
        dialogLayout.addView(iconLabel)
        dialogLayout.addView(iconContainer)

        AlertDialog.Builder(context)
            .setTitle("Nowy nawyk")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                createHabit(editText.text.toString())
            }
            .setNegativeButton("Anuluj", null)
            .setNeutralButton("Przykłady") { _, _ ->
                showExampleHabitsDialog()
            }
            .show()

        // Pokaż klawiaturę automatycznie
        editText.requestFocus()
    }

    private fun createIconSelector(): LinearLayout {
        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
        }

        // Twórz rzędy po 4 ikony (lepiej się mieszczą)
        val rows = availableIcons.chunked(4)

        rows.forEach { iconRow ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }

            iconRow.forEach { icon ->
                val iconButton = Button(context).apply {
                    text = icon
                    textSize = 24f // większy rozmiar emoji
                    layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { // większa wysokość
                        setMargins(8, 6, 8, 6) // większe marginesy
                    }
                    setPadding(4, 8, 4, 8) // lepszy padding

                    // Zaznacz domyślną ikonę
                    isSelected = icon == selectedIcon
                    updateIconButtonStyle(this)

                    setOnClickListener {
                        // Odznacz wszystkie w tym kontenerze
                        deselectAllIcons(mainLayout)

                        // Zaznacz ten
                        isSelected = true
                        selectedIcon = icon
                        updateIconButtonStyle(this)
                    }
                }
                rowLayout.addView(iconButton)
            }

            mainLayout.addView(rowLayout)
        }

        return mainLayout
    }

    private fun deselectAllIcons(container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val row = container.getChildAt(i) as LinearLayout
            for (j in 0 until row.childCount) {
                val button = row.getChildAt(j) as Button
                button.isSelected = false
                updateIconButtonStyle(button)
            }
        }
    }

    private fun updateIconButtonStyle(button: Button) {
        if (button.isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright))
            button.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            button.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    private fun createHabit(name: String) {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            Toast.makeText(context, "Wprowadź nazwę nawyku", Toast.LENGTH_SHORT).show()
            return
        }

        if (habitManager.isNameTaken(trimmedName)) {
            Toast.makeText(context, "Nawyk o tej nazwie już istnieje", Toast.LENGTH_SHORT).show()
            return
        }

        // Tworzymy nawyk z wybraną ikoną
        val habit = Habit(
            name = trimmedName,
            icon = selectedIcon, // używa wybranej ikony
            color = "#4CAF50", // domyślny zielony kolor
            createdDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        if (habitManager.addHabit(habit)) {
            habitManager.setActiveHabitId(habit.id)
            onHabitCreatedListener?.invoke(habit)
            Toast.makeText(context, "Utworzono nawyk: ${habit.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Błąd podczas tworzenia nawyku", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExampleHabitsDialog() {
        val examples = listOf(
            "💪 Ćwiczenia",
            "📚 Czytanie",
            "🏃 Bieganie",
            "🧘 Medytacja",
            "💧 Picie wody",
            "🌱 Nauka języka",
            "🚶 Spacer",
            "✍️ Pisanie dziennika",
            "🧹 Sprzątanie",
            "🎵 Nauka gitary"
        )

        AlertDialog.Builder(context)
            .setTitle("Wybierz przykładowy nawyk")
            .setItems(examples.toTypedArray()) { _, which ->
                val selectedExample = examples[which]
                val parts = selectedExample.split(" ", limit = 2)
                val icon = parts[0]
                val name = parts[1]
                val uniqueName = habitManager.generateUniqueName(name)

                selectedIcon = icon
                createHabit(uniqueName)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    /**
     * Ustawia listener utworzenia nawyku
     */
    fun setOnHabitCreatedListener(listener: (Habit) -> Unit) {
        onHabitCreatedListener = listener
    }
}