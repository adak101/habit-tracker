package com.example.habittracker.calendar

import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val dayOfMonth: Int,
    val date: String,           // "2025-06-18"
    val isCurrentMonth: Boolean, // czy dzień należy do aktualnego miesiąca
    val isToday: Boolean
)

object CalendarUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val polishMonths = arrayOf(
        "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
        "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
    )

    /**
     * Generuje grid 7x6 dni dla kalendarza miesięcznego
     * Zawiera dni z poprzedniego/następnego miesiąca dla wypełnienia
     */
    fun getMonthGrid(year: Int, month: Int): List<List<CalendarDay>> {
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(Date())

        // Pierwszy dzień miesiąca
        calendar.set(year, month, 1)
        val firstDayOfMonth = calendar.time

        // Pierwszy dzień tygodnia (poniedziałek = 1)
        val firstDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            else -> calendar.get(Calendar.DAY_OF_WEEK) - 1
        }

        // Ile dni w miesiącu
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Generuj wszystkie dni (42 = 6 tygodni x 7 dni)
        val allDays = mutableListOf<CalendarDay>()

        // Poprzedni miesiąc (wypełnienie)
        calendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in (firstDayOfWeek - 1) downTo 1) {
            val day = daysInPrevMonth - i + 1
            calendar.set(Calendar.DAY_OF_MONTH, day)
            allDays.add(CalendarDay(
                dayOfMonth = day,
                date = dateFormat.format(calendar.time),
                isCurrentMonth = false,
                isToday = false
            ))
        }

        // Aktualny miesiąc
        calendar.set(year, month, 1)
        for (day in 1..daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(calendar.time)
            allDays.add(CalendarDay(
                dayOfMonth = day,
                date = dateStr,
                isCurrentMonth = true,
                isToday = dateStr == today
            ))
        }

        // Następny miesiąc (wypełnienie)
        calendar.add(Calendar.MONTH, 1)
        var nextMonthDay = 1
        while (allDays.size < 42) {
            calendar.set(Calendar.DAY_OF_MONTH, nextMonthDay)
            allDays.add(CalendarDay(
                dayOfMonth = nextMonthDay,
                date = dateFormat.format(calendar.time),
                isCurrentMonth = false,
                isToday = false
            ))
            nextMonthDay++
        }

        // Podziel na tygodnie (7 dni każdy)
        return allDays.chunked(7)
    }

    /**
     * Formatuje miesiąc do wyświetlenia "Marzec 2025"
     */
    fun formatMonth(year: Int, month: Int): String {
        return "${polishMonths[month]} $year"
    }

    /**
     * Poprzedni miesiąc
     */
    fun getPreviousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 0) {
            Pair(year - 1, 11)
        } else {
            Pair(year, month - 1)
        }
    }

    /**
     * Następny miesiąc
     */
    fun getNextMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 11) {
            Pair(year + 1, 0)
        } else {
            Pair(year, month + 1)
        }
    }

    /**
     * Aktualny miesiąc i rok
     */
    fun getCurrentMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }
}