package no.nordicsemi.android.kotlin.ble.test

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice

@Module
@InstallIn(SingletonComponent::class)
class DevicesModule {

    @Provides
    internal fun provideServerDevice(): MockServerDevice {
        return MockServerDevice(
            name = "GLS Server",
            address = "55:44:33:22:11"
        )
    }

    @Provides
    internal fun provideClientDevice(): MockClientDevice {
        return MockClientDevice(
            name = "Mobile phone",
            address = "11:22:33:44:55"
        )
    }
}
