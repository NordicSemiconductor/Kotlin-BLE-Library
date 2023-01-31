package no.nordicsemi.android.kotlin.ble.client.nativ

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val ClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
