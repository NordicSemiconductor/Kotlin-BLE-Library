# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin BLE Library v2.0 — a Bluetooth Low Energy library for Android using Kotlin Coroutines. Provides native and mock implementations for scanning, connecting, and GATT operations. Currently Android-only (KMP in the name reflects future plans).

## Build Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :client-core-android:build

# Run unit tests
./gradlew :client-core-android:test

# Run instrumented tests (requires device/emulator)
./gradlew :test:connectedAndroidTest

# Install sample app
./gradlew sample:installNativeDebug   # real Bluetooth
./gradlew sample:installMockDebug     # simulated Bluetooth

# Generate documentation
./gradlew dokkaGenerate
```

## Architecture

### Layered Module Structure

The project follows a three-layer pattern: **core (JVM)** → **android (platform-specific)** → **mock (testing)**

```
Layer 1 - Platform-independent (JVM modules, no Android deps):
  core              → Environment, Manager, Peer interfaces
  client-core       → Generic CentralManager<ID,P,EX,F,SR>, Peripheral, GATT types
  advertiser-core   → Generic advertising API
  core-mock         → Mock implementations of core interfaces

Layer 2 - Android-specific:
  core-android            → AndroidEnvironment interface
  environment-android     → NativeAndroidEnvironment (wraps Android BT API)
  client-core-android     → Android CentralManager impl, GATT operations
  client-android          → Binds native impl with Android context
  advertiser-core-android → Android advertiser core
  advertiser-android      → Native advertiser with Android context

Layer 3 - Mock (for testing/previews):
  environment-android-mock  → MockAndroidEnvironment (simulates API 21-34, device quirks)
  client-core-mock          → MockCentralManager, MockPeripheral
  client-android-mock       → Combines mock client with mock environment
  advertiser-android-mock   → Mock advertiser
```

Dependencies flow downward: `client-android` → `client-core-android` → `client-core` → `core`.

### Key Patterns

- **Environment injection**: All Bluetooth access goes through `Environment` interface. Native vs mock is selected via product flavors in the sample app.
- **Generic type parameters**: `CentralManager<ID, P, EX, F, SR>` allows platform-specific types while sharing scanning/connection logic.
- **Coroutines-based API**: All async operations use `Flow` and `suspend` functions. Services exposed as `StateFlow`.
- **SLF4J logging**: Library modules use SLF4J; the sample app bridges to Timber.

### Sample App Flavors

The `sample` module uses product flavors (`native` / `mock`) to swap implementations via Hilt DI:
- `sample/src/native/` — provides `NativeAndroidEnvironment`
- `sample/src/mock/` — provides `MockAndroidEnvironment` with simulated peripherals

## Build System

- Gradle with Kotlin DSL
- Nordic Gradle Plugins (`no.nordicsemi.android.gradle`) for library/app configuration
- Two version catalogs: `libs` (from Nordic remote catalog) and `nordic` (local `gradle/nordic.versions.toml`)
- JVM modules use `nordic.kotlin.jvm` plugin; Android libraries use `nordic.library` + `nordic.kotlin.android`
- Publishing via `nordic.nexus.android` / `nordic.nexus.jvm` plugins

## Module Status

- **Active**: core, client, advertiser, environment modules, sample
- **Not yet implemented in v2.0**: server-core, server-core-android, server-android, server-android-mock (commented out in settings.gradle.kts)
- **Not migrated**: profile, test modules
