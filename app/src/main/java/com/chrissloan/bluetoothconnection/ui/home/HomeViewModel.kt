package com.chrissloan.bluetoothconnection.ui.home

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.Permission
import com.chrissloan.bluetoothconnection.usecases.bluetooth.BluetoothDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bluetoothDeviceRepository: BluetoothDeviceRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<HomeViewState>().apply {
        value = HomeViewState(isLoading = true)
    }
    val viewState: LiveData<HomeViewState> = _viewState

    val requiredPermissions = Permission.Bluetooth

    fun findBluetoothDevices() {
        Timber.d("Find Bluetooth Devices")
        viewModelScope.launch {
            val list = bluetoothDeviceRepository.getDeviceList()
            Timber.d("Inside scope : $list")
        }
    }

    fun permissionRequestDenied() {
        Timber.d("Permission Denied. We should show an error state")
    }
}

data class HomeViewState(
    val isLoading: Boolean = false,
    val shouldRequestPermissions: Boolean = false,
    val message: String? = null,
    val data: List<BluetoothDevice> = listOf(),
)
