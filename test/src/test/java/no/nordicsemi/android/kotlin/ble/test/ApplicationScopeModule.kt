package no.nordicsemi.android.kotlin.ble.test

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@Module
@InstallIn(SingletonComponent::class)
class ApplicationScopeModule {

    @Provides
    internal fun provideServerDevice(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
    }
}

