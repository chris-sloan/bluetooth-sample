package com.chrissloan.bluetoothconnection.usecases.bluetooth

import com.chrissloan.bluetoothconnection.dependencies.android.bluetooth.BluetoothDeviceManager
import timber.log.Timber
import javax.inject.Inject

class BluetoothDeviceRepository @Inject constructor(
    private val bluetoothDeviceManager: BluetoothDeviceManager
) {
    init {
        Timber.d("adapter is :$bluetoothDeviceManager")
    }

    suspend fun refreshDeviceList() {
        bluetoothDeviceManager.scanForDevices()
    }

    val deviceDiscoveryState = bluetoothDeviceManager.deviceDiscoveryState
    val devicesState = bluetoothDeviceManager.devicesState
}
