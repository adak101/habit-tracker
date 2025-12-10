package com.example.habittracker.habits

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

/**
 * Komponent do wyboru aktywnego nawyku
 */
class HabitSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var habitSpinner: Spinner
    private lateinit var addHabitButton: Button
    private lateinit var habitManager: HabitManager

    private var onHabitSelectedListener: ((Habit?) -> Unit)? = null
    private var onAddHabitClickListener: (() -> Unit)? = null

    private var habits: List<Habit> = emptyList()
    private var selectedHabit: Habit? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setupViews()
    }

    private fun setupViews() {
        // Spinner z nawykami
        habitSpinner = Spinner(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 16
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position < habits.size) {
                        selectedHabit = habits[position]
                        habitManager.setActiveHabitId(selectedHabit?.id ?: "")
                        onHabitSelectedListener?.invoke(selectedHabit)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedHabit = null
                    onHabitSelectedListener?.invoke(null)
                }
            }
        }

        // Przycisk dodawania nowego nawyku
        addHabitButton = Button(context).apply {
            text = "+"
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                width = 120
            }

            setOnClickListener {
                onAddHabitClickListener?.invoke()
            }
        }

        addView(habitSpinner)
        addView(addHabitButton)
    }

    /**
     * Inicjalizuje komponent z HabitManager
     */
    fun initialize(habitManager: HabitManager) {
        this.habitManager = habitManager
        refreshHabits()
    }

    /**
     * Odświeża listę nawyków
     */
    fun refreshHabits() {
        if (!::habitManager.isInitialized) return

        habits = habitManager.getActiveHabits()
        updateSpinnerAdapter()
        selectCurrentActiveHabit()
    }

    private fun updateSpinnerAdapter() {
        val adapter = HabitSpinnerAdapter(context, habits)
        habitSpinner.adapter = adapter

        // Aktualizuj stan przycisku
        addHabitButton.isEnabled = true

        if (habits.isEmpty()) {
            // Brak nawyków - pokaż informację
            habitSpinner.isEnabled = false
        } else {
            habitSpinner.isEnabled = true
        }
    }

    private fun selectCurrentActiveHabit() {
        if (habits.isEmpty()) return

        val activeHabitId = habitManager.getActiveHabitId()
        val activeIndex = habits.indexOfFirst { it.id == activeHabitId }

        if (activeIndex != -1) {
            habitSpinner.setSelection(activeIndex)
            selectedHabit = habits[activeIndex]
        } else {
            // Jeśli aktywny nawyk nie znaleziony, wybierz pierwszy
            if (habits.isNotEmpty()) {
                habitSpinner.setSelection(0)
                selectedHabit = habits[0]
                habitManager.setActiveHabitId(selectedHabit?.id ?: "")
            }
        }
    }

    /**
     * Pobiera aktualnie wybrany nawyk
     */
    fun getSelectedHabit(): Habit? = selectedHabit

    /**
     * Ustawia listener zmiany nawyku
     */
    fun setOnHabitSelectedListener(listener: (Habit?) -> Unit) {
        onHabitSelectedListener = listener
    }

    /**
     * Ustawia listener kliknięcia przycisku dodawania
     */
    fun setOnAddHabitClickListener(listener: () -> Unit) {
        onAddHabitClickListener = listener
    }

    /**
     * Wybiera nawyk o podanym ID
     */
    fun selectHabit(habitId: String) {
        val index = habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            habitSpinner.setSelection(index)
        }
    }
}

/**
 * Adapter dla Spinner z nawykami
 */
private class HabitSpinnerAdapter(
    private val context: Context,
    private val habits: List<Habit>
) : BaseAdapter() {

    override fun getCount(): Int = if (habits.isEmpty()) 1 else habits.size

    override fun getItem(position: Int): Any {
        return if (habits.isEmpty()) "Brak nawyków" else habits[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup?): View {
        return createView(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup?): View {
        return createView(position, convertView, parent, true)
    }

    private fun createView(position: Int, convertView: View?, parent: android.view.ViewGroup?, isDropDown: Boolean): View {
        val textView = TextView(context).apply {
            setPadding(16, 12, 16, 12)
            textSize = 18f  // Większa nazwa nawyku
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        if (habits.isEmpty()) {
            textView.text = "Brak nawyków - dodaj pierwszy"
            textView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        } else {
            val habit = habits[position]
            textView.text = "${habit.icon} ${habit.name}"
        }

        return textView
    }
}