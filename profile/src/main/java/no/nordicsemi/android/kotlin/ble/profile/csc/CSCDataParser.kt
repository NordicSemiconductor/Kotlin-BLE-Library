package no.nordicsemi.android.kotlin.ble.profile.csc

import no.nordicsemi.android.kotlin.ble.profile.common.Data
import no.nordicsemi.android.kotlin.ble.profile.error.InvalidDataReceived
import kotlin.experimental.and

class CSCDataParser {

    private var mInitialWheelRevolutions: Long = -1
    private var mLastWheelRevolutions: Long = -1
    private var mLastWheelEventTime = -1
    private var mLastCrankRevolutions = -1
    private var mLastCrankEventTime = -1
    private var mWheelCadence = -1f

    fun parse(byteArray: ByteArray) {
        val data = Data(byteArray)

        if (data.size() < 1) {
            throw InvalidDataReceived()
            return
        }

        // Decode the new data
        var offset = 0
        val flags: Byte = data.getByte(offset)!!
        offset += 1

        val wheelRevPresent = (flags and 0x01).toInt() != 0
        val crankRevPreset = (flags and 0x02).toInt() != 0

        if (data.size() < 1 + (if (wheelRevPresent) 6 else 0) + (if (crankRevPreset) 4 else 0)) {
            throw InvalidDataReceived()
            return
        }

        if (wheelRevPresent) {
            val wheelRevolutions: Long = data.getIntValue(Data.FORMAT_UINT32_LE, offset)!!.toLong() and 0xFFFFFFFFL
            offset += 4
            val lastWheelEventTime: Int = data.getIntValue(Data.FORMAT_UINT16_LE, offset)!! // 1/1024 s
            offset += 2
            if (mInitialWheelRevolutions < 0) mInitialWheelRevolutions = wheelRevolutions

            // Notify listener about the new measurement
            mLastWheelRevolutions = wheelRevolutions
            mLastWheelEventTime = lastWheelEventTime
        }

        if (crankRevPreset) {
            val crankRevolutions: Int = data.getIntValue(Data.FORMAT_UINT16_LE, offset)!!
            offset += 2
            val lastCrankEventTime: Int = data.getIntValue(Data.FORMAT_UINT16_LE, offset)!!
            // offset += 2;

            // Notify listener about the new measurement
            mLastCrankRevolutions = crankRevolutions
            mLastCrankEventTime = lastCrankEventTime
        }
    }
}

data class CSCDataSnapshot(

)
