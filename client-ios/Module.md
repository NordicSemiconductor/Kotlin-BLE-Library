# Module client-ios

iOS-specific module for Bluetooth LE client operations.

This module provides the native iOS implementation binding for the BLE client,
wrapping CoreBluetooth framework types (`CBCentralManager`, `CBPeripheral`) into
Kotlin Coroutines-based APIs. It depends on `client-core-ios` for type definitions
and `environment-ios` for the native iOS environment.
