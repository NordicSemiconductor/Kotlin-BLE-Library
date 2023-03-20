package no.nordicsemi.android.kotlin.ble.profile.prx

object AlertLevelInputParser {

    fun parse(alarmLevel: AlarmLevel): ByteArray {
        return alarmLevel.value
    }
}
