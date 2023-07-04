package no.nordicsemi.android.kotlin.ble.server.main

import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertContentEquals

class BleGattPropertyTest {

    @Test
    fun `when parse write property should success`() {
        val properties = listOf(BleGattProperty.PROPERTY_WRITE)

        val intValue = BleGattProperty.toInt(properties)

        assertEquals(BleGattProperty.createProperties(intValue), properties)
    }

    @Test
    fun `when parse write and indicate property should success`() {
        val properties = listOf(BleGattProperty.PROPERTY_INDICATE, BleGattProperty.PROPERTY_WRITE)

        val intValue = BleGattProperty.toInt(properties)

        assertContentEquals(BleGattProperty.createProperties(intValue), properties)
    }
}
