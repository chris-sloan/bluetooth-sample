package com.chrissloan.bluetoothconnection.dependencies.android.bluetooth

import android.annotation.SuppressLint
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("MissingPermission")
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

    private var startedIntent: Intent? = null // TODO Work out if needed
    private var finishedIntent: Intent? = null // TODO Work out if needed

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

    private var _deviceState: DevicesState = DevicesState.BluetoothDevicesFound(emptyList())
    val devicesState: Flow<DevicesState> = flow {
        Timber.i("Inside DeviceState flow ")
        while (true) {
            Timber.i("About to emit $_deviceState")
            emit(_deviceState)
            delay (1000)
        }
    }

    private val  discoveryStateChannel = Channel<DeviceDiscoveryState>()
    private var _deviceDiscoveryState: DeviceDiscoveryState = DeviceDiscoveryState.Idle
    val deviceDiscoveryState: Flow<DeviceDiscoveryState> = flow {
        Timber.i("inside flow state is : $_deviceDiscoveryState")
        while(true) {

            emit(_deviceDiscoveryState)
            delay(1000)
        }
    }

    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private fun onDeviceDiscoveryStarted() {
        Timber.i("onDeviceDiscoveryStarted")
        discoveryStateChannel.trySend(DeviceDiscoveryState.Searching)
    }

    private fun onDeviceDiscoveryFinished() {
        Timber.i("onDeviceDiscoveryFinished")
        _deviceDiscoveryState = DeviceDiscoveryState.Idle
        _deviceState = DevicesState.BluetoothDevicesFound(deviceList)
        cleanUp()
    }

    suspend fun scanForDevices() {
        Timber.i("Scan for devices")
        withContext(Dispatchers.IO) {
            checkPermissions()
            checkBluetoothIsEnabled()
            deviceList.clear()
            deviceList.addAll(bluetoothAdapter?.bondedDevices.orEmpty())
            broadcastRegistrar.registerReceiver(receiver, filter)
            bluetoothAdapter?.startDiscovery()
        }
    }

    private fun checkBluetoothIsEnabled() {
        if (bluetoothAdapter?.isEnabled == false) {
            _deviceState = DevicesState.BluetoothNotEnabled
        }
    }

    private fun checkPermissions() {
        if (!deviceCapabilityChecker.hasDeviceCapability(PackageManager.FEATURE_BLUETOOTH)) {
            _deviceState = DevicesState.BluetoothNotAvailable
        }
        if (!deviceCapabilityChecker.hasDeviceCapability(PackageManager.FEATURE_BLUETOOTH_LE)) {
            _deviceState = DevicesState.BluetoothLENotAvailable
        }
    }

    private fun cleanUp() {
        bluetoothAdapter?.cancelDiscovery()
        broadcastRegistrar.unregisterReceiver(receiver)
    }

    sealed class DeviceDiscoveryState {
        object Idle : DeviceDiscoveryState()
        object Searching : DeviceDiscoveryState()
    }

    sealed class DevicesState {
        object BluetoothNotAvailable : DevicesState()
        object BluetoothLENotAvailable : DevicesState()
        object BluetoothNotEnabled : DevicesState()
        class BluetoothDevicesFound(val devices: List<BluetoothDevice>) : DevicesState()
    }
}
