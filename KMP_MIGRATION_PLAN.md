# KMP Migration Plan: iOS Support

## Overview

Migrate Kotlin BLE Library from Android-only to Kotlin Multiplatform with iOS support.
The strategy: convert Layer 1 (JVM modules) to KMP common, keep Layer 2 Android as-is, add Layer 2 iOS.

---

## Current Architecture

```
Layer 1 - Platform-independent (JVM, no Android deps):
  core              -> Environment, Manager, Peer interfaces
  client-core       -> Generic CentralManager<ID,P,EX,F,SR>, Peripheral, GATT types
  advertiser-core   -> Generic advertising API
  core-mock         -> Mock implementations of core interfaces
  client-core-mock  -> Mock CentralManager, MockPeripheral

Layer 2 - Android-specific:
  core-android            -> AndroidEnvironment interface
  environment-android     -> NativeAndroidEnvironment (wraps Android BT API)
  client-core-android     -> Android CentralManager impl, GATT operations
  client-android          -> Binds native impl with Android context
  advertiser-core-android -> Android advertiser core
  advertiser-android      -> Native advertiser with Android context

Layer 3 - Mock (testing/previews):
  environment-android-mock  -> MockAndroidEnvironment
  client-android-mock       -> MockCentralManager, MockPeripheral
  advertiser-android-mock   -> Mock advertiser
```

---

## Phase 1: Infrastructure Setup

### 1.1 Add KMP plugin to root `build.gradle.kts`

Add `kotlin("multiplatform")` plugin (apply false).

### 1.2 Create convention plugin for KMP modules

Either adapt Nordic Gradle Plugins or create a custom convention plugin that configures:

```kotlin
kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
```

### 1.3 Configure iOS targets

Define iOS targets with framework export for consumption from Swift/Obj-C.

---

## Phase 2: Migrate `core` Module (JVM -> KMP Common)

**Status:** Layer 1 modules have ZERO Android imports. Pure Kotlin + kotlinx.

### 2.1 `core` module

1. Change plugin: `nordic.kotlin.jvm` -> `kotlin("multiplatform")`
2. Move sources: `src/main/java/` -> `src/commonMain/kotlin/`
3. Remove `src/main/AndroidManifest.xml` (or move to `androidMain`)
4. Fix JVM-specific imports:
   - `java.io.Closeable` -> `kotlin.io.Closeable` or custom `expect/actual`
   - `java.util.Locale` -> `expect/actual` or remove
5. Update `build.gradle.kts`:
   ```kotlin
   kotlin {
       jvm()
       iosX64()
       iosArm64()
       iosSimulatorArm64()

       sourceSets {
           commonMain.dependencies {
               implementation(libs.kotlinx.coroutines.core)
           }
       }
   }
   ```
6. Verify Android modules still compile against `core`

### 2.2 `core-mock` module

1. Same plugin/source migration as `core`
2. Dependency: `api(project(":core"))` stays the same

### 2.3 Verify

Run `./gradlew :core:build` and `./gradlew :core-mock:build` for all targets.
Run `./gradlew :core-android:build` to confirm Android modules still work.

---

## Phase 3: Migrate `client-core` and `advertiser-core`

### 3.1 `client-core` module

1. Same plugin/source migration
2. Replace SLF4J with KMP-compatible logging:
   - Option A: Kermit (Touchlab) — mature KMP logging library
   - Option B: `expect/actual` wrapper:
     ```kotlin
     // commonMain
     expect fun logger(name: String): Logger

     // androidMain / jvmMain
     actual fun logger(name: String): Logger = SLF4J bridge

     // iosMain
     actual fun logger(name: String): Logger = NSLog / os_log bridge
     ```
3. Dependencies: `kotlinx.datetime` — already KMP-compatible

### 3.2 `client-core-mock` module

1. Same plugin/source migration
2. Dependencies: `api(project(":core-mock"))`, `api(project(":client-core"))` — both already KMP

### 3.3 `advertiser-core` module

1. Same plugin/source migration
2. Minimal dependencies — only depends on `:core`

### 3.4 Verify

Run builds for all targets. Verify Android Layer 2 modules still compile.

---

## Phase 4: Create Layer 2 iOS Modules

### 4.1 Module mapping

| Android module            | New iOS module           | CoreBluetooth equivalent               |
|---------------------------|--------------------------|----------------------------------------|
| `environment-android`     | `environment-ios`        | `CBManager.state`, `CBManager.authorization` |
| `client-core-android`     | `client-core-ios`        | iOS client abstractions                |
| `client-android`          | `client-ios`             | `CBCentralManager`, `CBPeripheral`     |
| `advertiser-core-android` | `advertiser-core-ios`    | iOS advertiser abstractions            |
| `advertiser-android`      | `advertiser-ios`         | `CBPeripheralManager`                  |

### 4.2 `environment-ios`

Implements `Environment` interface using:
- `CBManager.state` — Bluetooth state (poweredOn, poweredOff, etc.)
- `CBManager.authorization` — authorization status
- No location permissions needed (unlike Android)

### 4.3 `client-ios`

Implements `CentralManager` and `Peripheral` using CoreBluetooth:

**Scanning:**
- `CBCentralManager.scanForPeripherals(withServices:options:)`
- `CBCentralManagerDelegate.centralManager(_:didDiscover:advertisementData:rssi:)`

**Connection:**
- `CBCentralManager.connect(_:options:)`
- `CBCentralManagerDelegate.centralManager(_:didConnect:)`
- `CBCentralManagerDelegate.centralManager(_:didFailToConnect:error:)`
- `CBCentralManagerDelegate.centralManager(_:didDisconnectPeripheral:error:)`

**GATT Operations (via CBPeripheralDelegate):**
- `CBPeripheral.discoverServices(_:)`
- `CBPeripheral.discoverCharacteristics(_:for:)`
- `CBPeripheral.readValue(for:)` — read characteristic
- `CBPeripheral.writeValue(_:for:type:)` — write characteristic
- `CBPeripheral.setNotifyValue(_:for:)` — enable notifications
- `CBPeripheral.readValue(for:)` — read descriptor
- `CBPeripheral.writeValue(_:for:)` — write descriptor
- `CBPeripheral.readRSSI()` — read RSSI
- `CBPeripheral.maximumWriteValueLength(for:)` — MTU equivalent (read-only)

**CoreBluetooth delegate -> Flow/suspend conversion:**
```kotlin
// Wrap delegate callbacks in callbackFlow or suspendCancellableCoroutine
fun scanForPeripherals(): Flow<ScanResult> = callbackFlow {
    centralManager.scanForPeripherals(withServices = null, options = null)
    // delegate callback sends to channel
    awaitClose { centralManager.stopScan() }
}
```

### 4.4 `advertiser-ios`

Implements `BluetoothLeAdvertiser` using:
- `CBPeripheralManager.startAdvertising(_:)`
- `CBPeripheralManager.stopAdvertising()`
- `CBPeripheralManagerDelegate.peripheralManagerDidStartAdvertising(_:error:)`

### 4.5 Mock modules for iOS

The existing mock modules (`client-core-mock`, `core-mock`) are platform-independent after Phase 2-3 migration.
iOS-specific mock modules may not be needed if mock modules become common KMP modules.

---

## Android BLE API -> iOS CoreBluetooth Mapping

| Operation | Android | iOS (CoreBluetooth) |
|-----------|---------|---------------------|
| Scan | `BluetoothLeScanner.startScan()` | `CBCentralManager.scanForPeripherals()` |
| Connect | `BluetoothDevice.connectGatt()` | `CBCentralManager.connect(peripheral)` |
| Discover services | `BluetoothGatt.discoverServices()` | `CBPeripheral.discoverServices()` |
| Read characteristic | `BluetoothGatt.readCharacteristic()` | `CBPeripheral.readValue(for:)` |
| Write characteristic | `BluetoothGatt.writeCharacteristic()` | `CBPeripheral.writeValue(for:type:)` |
| Notifications | `setCharacteristicNotification()` + CCCD write | `CBPeripheral.setNotifyValue(for:)` |
| MTU | `BluetoothGatt.requestMtu()` | `CBPeripheral.maximumWriteValueLength()` (read-only) |
| PHY | `BluetoothGatt.setPreferredPhy()` | Not supported in CoreBluetooth |
| Bonding | `BluetoothDevice.createBond()` | Automatic (iOS manages pairing) |
| Reliable write | `BluetoothGatt.beginReliableWrite()` | Not supported in CoreBluetooth |
| Connection priority | `requestConnectionPriority()` | Not supported in CoreBluetooth |
| RSSI | `BluetoothGatt.readRemoteRssi()` | `CBPeripheral.readRSSI()` |
| Advertising | `BluetoothLeAdvertiser.startAdvertising()` | `CBPeripheralManager.startAdvertising()` |

---

## Technical Challenges

### 1. SLF4J Replacement

SLF4J does not support iOS. Options:
- **Kermit** (Touchlab) — mature KMP logging library
- Custom `expect/actual` Logger interface

### 2. CoreBluetooth Delegate Pattern

iOS BLE uses delegate callbacks, not listeners. Must wrap in coroutines:
- `callbackFlow {}` for streaming operations (scan, notifications)
- `suspendCancellableCoroutine {}` for one-shot operations (read, write)

### 3. Missing iOS Equivalents

Some Android BLE features don't exist on iOS:
- `requestMtu()` — iOS negotiates automatically, only read via `maximumWriteValueLength`
- `setPreferredPhy()` — not exposed in CoreBluetooth
- `createBond()` — iOS manages pairing automatically
- `reliableWrite` — not supported
- `requestConnectionPriority()` — not supported

**Strategy:** Make these operations optional in common interfaces, or throw `UnsupportedOperationException` on iOS.

### 4. UUID Format

Android uses 128-bit UUID strings, iOS uses `CBUUID` (supports 16/32/128-bit).
`kotlin.uuid.Uuid` (already used in the project) works on both platforms.

### 5. Nordic Gradle Plugins

Current plugins (`nordic.kotlin.jvm`, `nordic.library`, etc.) are Android-focused.
Options:
- Adapt Nordic plugins to support KMP
- Replace with standard `kotlin("multiplatform")` + custom convention plugins

### 6. Permissions Model

Android requires explicit BLE permissions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`).
iOS requires `NSBluetoothAlwaysUsageDescription` in Info.plist + runtime authorization via `CBManager.authorization`.
The `Environment` interface should abstract this difference.

---

## Target Module Structure (After Migration)

```
Layer 1 - KMP Common (commonMain):
  core              -> Environment, Manager, Peer interfaces
  client-core       -> Generic CentralManager, Peripheral, GATT types
  advertiser-core   -> Generic advertising API
  core-mock         -> Mock implementations of core interfaces
  client-core-mock  -> Mock CentralManager, MockPeripheral

Layer 2 - Android-specific (unchanged):
  core-android            -> AndroidEnvironment interface
  environment-android     -> NativeAndroidEnvironment
  client-core-android     -> Android CentralManager impl
  client-android          -> Binds native impl with Android context
  advertiser-core-android -> Android advertiser core
  advertiser-android      -> Native advertiser with Android context

Layer 2 - iOS-specific (NEW):
  environment-ios         -> iOSEnvironment (CBManager state/auth)
  client-ios              -> CBCentralManager + CBPeripheral wrapper
  advertiser-ios          -> CBPeripheralManager wrapper

Layer 3 - Mock:
  environment-android-mock  -> MockAndroidEnvironment (keep as-is)
  client-android-mock       -> MockCentralManager (keep as-is)
  advertiser-android-mock   -> Mock advertiser (keep as-is)
  (iOS mocks may reuse common mock modules)
```

---

## Execution Order

1. Phase 1: Infrastructure (Gradle plugins, targets)
2. Phase 2: `core` + `core-mock` -> KMP
3. Phase 3: `client-core` + `client-core-mock` + `advertiser-core` -> KMP
4. Phase 4: iOS modules (`environment-ios`, `client-ios`, `advertiser-ios`)
5. Phase 5: iOS sample app (optional, SwiftUI)
