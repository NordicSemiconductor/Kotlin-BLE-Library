# Module environment-android

The native Android environment implementation.

## Overview

The `NativeAndroidEnvironment` object acts as a bridge between the app and the Android OS.

By having an additional abstraction layer, it is possible to use the native implementation
in the production app, and mock in tests without changing the logic or the UI.

## Usage

Whether you use the library as a client or a server, the first thing to need to do is to set up
the Environment.

For that, add the following dependency to your *build.gradle.kts* file:
```kotlin
implementation("no.nordicsemi.kotlin.ble:environment-android:<VERSION>")
```

**Note:** The library is also available using the *Nordic BOM* or *Version Catalog* from [Nordic Version Catalog](https://github.com/nordicsemi/Nordic-Version-Catalog).

With that complete, get an instance of the native Environment:

```kotlin
val environment = NativeAndroidEnvironment.getInstance(
    // Application context to access Android API.
    context = context,
    // Check your AndroidManifest and the flags in BLUETOOTH_SCAN permission
    // and set whether 'neverForLocation' flag was set.
    isNeverForLocationFlagSet = true
)
```

Make sure to close the Environment when it is no longer needed.

```kotlin
environment.close()
```

The `NativeAndroidEnvironment` registers `BroadcastReceiver`s, which must be unregistered before the
app is closed.

### Hilt

This is an example of how the `NativeAndroidEnvironment` can be initialized and closed using 
the `ActivityRetainedComponent` using *Hilt*:

```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
object EnvironmentModule {

    @ActivityRetainedScoped
    @Provides
    fun provideEnvironment(
        @ApplicationContext context: Context,
        lifecycle: ActivityRetainedLifecycle
    ): MockAndroidEnvironment {
        return MockAndroidEnvironment.getInstance(context, isNeverForLocationFlagSet = true)
            // Make sure the environment is closed when the lifecycle is cleared.
            // This will unregister the broadcast receiver.
            .also { env ->
                lifecycle.addOnClearedListener { env.close() }
            }
    }
}
```

Provide a generic binding to cast the `NativeAndroidEnvironment` to the `AndroidEnvironment` interface:
```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class AndroidEnvironmentModule {
    
    @Binds
    abstract fun bindEnvironment(environment: NativeAndroidEnvironment): AndroidEnvironment
}
```

# Package no.nordicsemi.kotlin.ble.environment.android

This package contains the implementation of native Android environment.