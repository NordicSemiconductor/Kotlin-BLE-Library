package no.nordicsemi.kotlin.ble.client.ios

import platform.CoreBluetooth.CBUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class IOSScanFilter : IOSScanFilterScope {
    private var nameExact: String? = null
    private var nameRegex: Regex? = null
    private val _serviceUuids: MutableList<CBUUID> = mutableListOf()

    val serviceUuids: List<CBUUID>?
        get() = _serviceUuids.ifEmpty { null }

    override fun Name(name: String) {
        nameExact = name
    }

    override fun Name(regex: Regex) {
        nameRegex = regex
    }

    override fun ServiceUuid(uuid: Uuid) {
        _serviceUuids.add(CBUUID.UUIDWithString(uuid.toString()))
    }

    fun matches(advertisingData: IOSAdvertisingData, peripheralName: String?): Boolean {
        nameExact?.let { expected ->
            val advName = advertisingData.name ?: peripheralName
            if (advName != expected) return false
        }
        nameRegex?.let { regex ->
            val advName = advertisingData.name ?: peripheralName ?: return false
            if (!regex.matches(advName)) return false
        }
        return true
    }
}
