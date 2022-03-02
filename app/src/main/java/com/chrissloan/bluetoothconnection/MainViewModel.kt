package com.chrissloan.bluetoothconnection

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrissloan.bluetoothconnection.dependencies.android.bluetooth.BluetoothDeviceManager.DevicesState.*
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.Permission
import com.chrissloan.bluetoothconnection.usecases.bluetooth.BluetoothDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothDeviceRepository: BluetoothDeviceRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>().apply {
        value = ViewState()
    }
    val viewState: LiveData<ViewState> = _viewState

    val requiredPermissions = Permission.Bluetooth

    init {
        Timber.i("VM init")
        observeDeviceDiscoveryState()
        observeDeviceState()
        Timber.i("Refreshing BT devices")
        refreshBluetoothDevices()
    }

    fun refreshBluetoothDevices() {
        Timber.d("Find Bluetooth Devices")
        viewModelScope.launch {
            bluetoothDeviceRepository.refreshDeviceList()
        }
    }

    private fun observeDeviceDiscoveryState() {
        Timber.d("deviceDiscoveryState : ")
        viewModelScope.launch {
            bluetoothDeviceRepository
                .deviceDiscoveryState
                .collect {
                    Timber.i("Incoming device discovery state: $it")
                    _viewState.value = ViewState(isLoading = true, data = emptyList())
                }
        }
    }

    private fun observeDeviceState() {
        viewModelScope.launch {
            bluetoothDeviceRepository
                .devicesState
                .collect {
                    Timber.i("Incoming device state: $it")
                    when (it) {
                        is BluetoothDevicesFound -> _viewState.value = ViewState(data = it.devices)
                        BluetoothLENotAvailable -> _viewState.value =
                            ViewState(message = "Bluetooth LE is not available")
                        BluetoothNotAvailable -> _viewState.value =
                            ViewState(message = "Bluetooth is not available")
                        BluetoothNotEnabled -> _viewState.value =
                            ViewState(message = "Bluetooth is not enabled")
                    }
                }
        }
    }

    fun permissionRequestDenied() {
        _viewState.value =
            ViewState(message = "Permissions Denied.")
    }
}

data class ViewState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val data: List<BluetoothDevice> = listOf(),
)
