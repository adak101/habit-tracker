package com.example.habittracker.calendar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.*
import java.util.*

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    private lateinit var monthHeader: TextView
    private lateinit var calendarGrid: TableLayout
    private var onDayClickListener: ((String) -> Unit)? = null
    private var dayStatuses: Map<String, Boolean?> = emptyMap()

    init {
        orientation = VERTICAL
        setupCalendar()
        initializeToCurrentMonth()
    }

    private fun setupCalendar() {
        // Header z nazwą miesiąca i nawigacją
        val headerLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 8)
        }

        val prevButton = Button(context).apply {
            text = "◀"
            textSize = 18f
            setOnClickListener { goToPreviousMonth() }
        }

        monthHeader = TextView(context).apply {
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nextButton = Button(context).apply {
            text = "▶"
            textSize = 18f
            setOnClickListener { goToNextMonth() }
        }

        headerLayout.addView(prevButton)
        headerLayout.addView(monthHeader)
        headerLayout.addView(nextButton)

        // Nagłówki dni tygodnia
        val weekHeaderLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(1, 8, 1, 8)
        }

        val dayNames = arrayOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nie")
        dayNames.forEach { dayName ->
            val dayHeader = TextView(context).apply {
                text = dayName
                gravity = Gravity.CENTER
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(140, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(1, 0, 1, 0)
                }
            }
            weekHeaderLayout.addView(dayHeader)
        }

        // Grid kalendarza
        calendarGrid = TableLayout(context).apply {
            setPadding(8, 0, 8, 8)
        }

        addView(headerLayout)
        addView(weekHeaderLayout)
        addView(calendarGrid)
    }

    private fun initializeToCurrentMonth() {
        val (year, month) = CalendarUtils.getCurrentMonth()
        currentYear = year
        currentMonth = month
        updateCalendar()
    }

    private fun updateCalendar() {
        monthHeader.text = CalendarUtils.formatMonth(currentYear, currentMonth)
        populateCalendarGrid()
    }

    private fun populateCalendarGrid() {
        calendarGrid.removeAllViews()

        val monthGrid = CalendarUtils.getMonthGrid(currentYear, currentMonth)

        monthGrid.forEach { week ->
            val tableRow = TableRow(context)

            week.forEach { day ->
                val dayTile = createDayTile(day)
                tableRow.addView(dayTile)
            }

            calendarGrid.addView(tableRow)
        }
    }

    private fun createDayTile(day: CalendarDay): FrameLayout {
        val tileSize = 140

        val frameLayout = FrameLayout(context).apply {
            layoutParams = TableRow.LayoutParams(tileSize, tileSize).apply {
                setMargins(1, 1, 1, 1)
            }
            setPadding(2, 2, 2, 2)

            // Tło i klikalność
            when {
                day.isToday -> {
                    setBackgroundColor(Color.parseColor("#E3F2FD"))
                    if (day.isCurrentMonth) {
                        setOnClickListener {
                            Log.d("CalendarView", "Kliknięto dzień: ${day.date}")
                            onDayClickListener?.invoke(day.date)
                        }
                        isClickable = true
                    } else {
                        isClickable = false
                    }
                }
                day.isCurrentMonth -> {
                    setBackgroundColor(Color.parseColor("#FAFAFA"))
                    setOnClickListener {
                        Log.d("CalendarView", "Kliknięto dzień: ${day.date}")
                        onDayClickListener?.invoke(day.date)
                    }
                    isClickable = true
                }
                else -> {
                    setBackgroundColor(Color.parseColor("#F5F5F5"))
                    isClickable = false
                }
            }
        }

        val dayNumber = TextView(context).apply {
            text = day.dayOfMonth.toString()
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(
                if (day.isCurrentMonth) Color.BLACK
                else Color.GRAY
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = 8
            }
        }

        val statusText = TextView(context).apply {
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val status = dayStatuses[day.date]
            text = when {
                !day.isCurrentMonth -> ""
                status == true -> "✓"
                status == false -> "✗"
                else -> ""
            }

            setTextColor(
                when (status) {
                    true -> Color.parseColor("#4CAF50")
                    false -> Color.parseColor("#F44336")
                    else -> Color.TRANSPARENT
                }
            )
        }

        frameLayout.addView(dayNumber)
        frameLayout.addView(statusText)

        return frameLayout
    }

    private fun goToPreviousMonth() {
        val (newYear, newMonth) = CalendarUtils.getPreviousMonth(currentYear, currentMonth)
        currentYear = newYear
        currentMonth = newMonth
        updateCalendar()
    }

    private fun goToNextMonth() {
        val (newYear, newMonth) = CalendarUtils.getNextMonth(currentYear, currentMonth)
        currentYear = newYear
        currentMonth = newMonth
        updateCalendar()
    }

    fun setOnDayClickListener(listener: (String) -> Unit) {
        onDayClickListener = listener
    }

    fun updateDayStatuses(statuses: Map<String, Boolean?>) {
        dayStatuses = statuses
        populateCalendarGrid()
    }

    fun getCurrentMonthYear(): Pair<Int, Int> {
        return Pair(currentYear, currentMonth)
    }
}
