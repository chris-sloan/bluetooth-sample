package com.chrissloan.bluetoothconnection.usecases.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.chrissloan.bluetoothconnection.dependencies.android.bluetooth.BluetoothDeviceManager
import com.chrissloan.bluetoothconnection.dependencies.android.bluetooth.BluetoothDeviceManager.*
import com.chrissloan.bluetoothconnection.dependencies.android.bluetooth.BluetoothDeviceManager.DevicesState.*
import javax.inject.Inject
import timber.log.Timber

class BluetoothDeviceRepository @Inject constructor(
    private val bluetoothDeviceManager: BluetoothDeviceManager
) {
    init {
        Timber.d("adapter is :$bluetoothDeviceManager")
    }

    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    fun getDeviceList(): List<BluetoothDevice> {
        Timber.d("getDeviceList")
        bluetoothDeviceManager.scanForDevices { devicesState ->
            Timber.d("incoming device state : $devicesState")
            when (devicesState) {
                is BluetoothDevicesFound -> deviceList.addAll(devicesState.devices)
                BluetoothLENotAvailable -> Unit
                BluetoothNotAvailable -> Unit
                BluetoothNotEnabled -> Unit
                BluetoothDiscoveryStarted -> Unit
            }

        }
        return deviceList
    }
}
