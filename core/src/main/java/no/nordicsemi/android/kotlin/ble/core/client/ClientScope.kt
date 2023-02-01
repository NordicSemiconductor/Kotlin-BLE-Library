package no.nordicsemi.android.kotlin.ble.core.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val ClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
