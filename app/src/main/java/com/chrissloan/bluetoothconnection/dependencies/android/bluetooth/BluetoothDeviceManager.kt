package com.chrissloan.bluetoothconnection.dependencies.android.bluetooth

import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.chrissloan.bluetoothconnection.dependencies.android.broadcast.BroadcastRegistrar
import com.chrissloan.bluetoothconnection.dependencies.android.capabilities.DeviceCapabilityChecker
import javax.inject.Inject
import timber.log.Timber

class BluetoothDeviceManager @Inject constructor(
    bluetoothManager: BluetoothManager,
    private val deviceCapabilityChecker: DeviceCapabilityChecker,
    private val broadcastRegistrar: BroadcastRegistrar,
) {
    private val bluetoothAdapter = bluetoothManager.adapter
    private val filter = IntentFilter().apply {
        this.addAction(ACTION_DISCOVERY_STARTED)
        this.addAction(ACTION_DISCOVERY_FINISHED)
        this.addAction(BluetoothDevice.ACTION_FOUND)
    }

    private var startedIntent: Intent? = null
    private var finishedIntent: Intent? = null

    private var deviceDiscoveryCallback: ((DevicesState) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("onReceive : ${intent.action}")
            when (intent.action) {
                ACTION_DISCOVERY_STARTED -> {
                    startedIntent = intent
                    onDeviceDiscoveryStarted()
                }
                ACTION_DISCOVERY_FINISHED -> {
                    finishedIntent = intent
                    onDeviceDiscoveryFinished()
                }
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { deviceList.add(it) }
                    Timber.d("Found device : ${device?.name} - ${device?.type} - ${device?.address}")
                }
            }
        }
    }

    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private fun onDeviceDiscoveryStarted() {
        deviceDiscoveryCallback?.invoke(DevicesState.BluetoothDiscoveryStarted)
    }

    private fun onDeviceDiscoveryFinished() {
        deviceDiscoveryCallback?.invoke(DevicesState.BluetoothDevicesFound(deviceList.toList()))
    }

    fun scanForDevices(callback: (DevicesState) -> Unit) {
        if (!deviceCapabilityChecker.hasDeviceCapability(PackageManager.FEATURE_BLUETOOTH)) {
            callback.invoke(DevicesState.BluetoothNotAvailable)
            return
        }
        if (!deviceCapabilityChecker.hasDeviceCapability(PackageManager.FEATURE_BLUETOOTH_LE)) {
            callback.invoke(DevicesState.BluetoothLENotAvailable)
            return
        }

        broadcastRegistrar.registerReceiver(receiver, filter)

        if (bluetoothAdapter?.isEnabled == false) {
            callback.invoke(DevicesState.BluetoothNotEnabled)
            return
        }

        deviceDiscoveryCallback = callback

        deviceList.clear()
        deviceList.addAll(bluetoothAdapter?.bondedDevices.orEmpty())
        bluetoothAdapter?.startDiscovery()
    }


    fun cleanUp() {
        bluetoothAdapter?.cancelDiscovery()
        broadcastRegistrar.unregisterReceiver(receiver)
    }

    sealed class DevicesState {
        object BluetoothNotAvailable : DevicesState()
        object BluetoothLENotAvailable : DevicesState()
        object BluetoothNotEnabled : DevicesState()
        object BluetoothDiscoveryStarted : DevicesState()
        class BluetoothDevicesFound(val devices: List<BluetoothDevice>) : DevicesState()
    }
}
