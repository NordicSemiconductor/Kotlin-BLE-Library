package no.nordicsemi.android.kotlin.ble.profile.hts

import no.nordicsemi.android.kotlin.ble.profile.common.Data
import java.util.*

internal object DateTimeParser {

    fun parse(data: Data, offset: Int): Calendar? {
        if (data.size() < offset + 7) return null
        val calendar = Calendar.getInstance()
        val year: Int = data.getIntValue(Data.FORMAT_UINT16_LE, offset) ?: return null
        val month: Int = data.getIntValue(Data.FORMAT_UINT8, offset + 2) ?: return null
        val day: Int = data.getIntValue(Data.FORMAT_UINT8, offset + 3) ?: return null
        val hourOfDay: Int = data.getIntValue(Data.FORMAT_UINT8, offset + 4) ?: return null
        val minute: Int = data.getIntValue(Data.FORMAT_UINT8, offset + 5) ?: return null
        val second: Int = data.getIntValue(Data.FORMAT_UINT8, offset + 6) ?: return null

        if (year > 0) {
            calendar[Calendar.YEAR] = year
        } else {
            calendar.clear(Calendar.YEAR)
        }

        if (month > 0) {
            calendar[Calendar.MONTH] = month - 1
        } else {
            calendar.clear(Calendar.MONTH)
        }

        if (day > 0) {
            calendar[Calendar.DATE] = day
        } else {
            calendar.clear(Calendar.DATE)
        }

        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = second
        calendar[Calendar.MILLISECOND] = 0

        return calendar
    }
}
