package no.nordicsemi.android.kotlin.ble.app.mock

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
class HiltModule {

    @Provides
    fun providesScope(): CoroutineScope = CoroutineScope(SupervisorJob())
}