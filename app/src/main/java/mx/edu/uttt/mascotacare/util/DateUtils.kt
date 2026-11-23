package mx.edu.uttt.mascotacare.util

import java.util.Calendar
import java.util.TimeZone

/** DatePicker entrega milisegundos a medianoche UTC. */
fun utcCalendar(millis: Long): Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    timeInMillis = millis
}

fun dateTimeAtLocalTime(dateMillis: Long, hour: Int, minute: Int): Long {
    val utcDate = utcCalendar(dateMillis)
    return Calendar.getInstance().apply {
        clear()
        set(
            utcDate.get(Calendar.YEAR), utcDate.get(Calendar.MONTH), utcDate.get(Calendar.DAY_OF_MONTH),
            hour, minute, 0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun isDateBeforeToday(dateMillis: Long): Boolean {
    val date = utcCalendar(dateMillis)
    val today = Calendar.getInstance()
    val targetKey = date.get(Calendar.YEAR) * 10_000 + date.get(Calendar.DAY_OF_YEAR)
    val todayKey = today.get(Calendar.YEAR) * 10_000 + today.get(Calendar.DAY_OF_YEAR)
    return targetKey < todayKey
}

fun isDateAfterToday(dateMillis: Long): Boolean {
    val date = utcCalendar(dateMillis)
    val today = Calendar.getInstance()
    val targetKey = date.get(Calendar.YEAR) * 10_000 + date.get(Calendar.DAY_OF_YEAR)
    val todayKey = today.get(Calendar.YEAR) * 10_000 + today.get(Calendar.DAY_OF_YEAR)
    return targetKey > todayKey
}

fun sameUtcDay(millis: Long, year: Int, month: Int, day: Int): Boolean {
    val date = utcCalendar(millis)
    return date.get(Calendar.YEAR) == year && date.get(Calendar.MONTH) == month && date.get(Calendar.DAY_OF_MONTH) == day
}
