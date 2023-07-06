package no.nordicsemi.android.kotlin.ble.mock.test

import no.nordicsemi.android.kotlin.ble.core.mapper.ScanRecordSerializer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContentEquals

@RunWith(RobolectricTestRunner::class)
internal class BleScanRecordParserTest {

    @Test
    fun `when serializing and deserializing ScanRecord should return same value`() {
        val rawData = TestData.scanRecord

        val record = ScanRecordSerializer.parseFromBytes(rawData)!!
        val resultRawData = ScanRecordSerializer.parseToBytes(record)

        assertContentEquals(rawData, resultRawData)
    }
}
