package com.chrissloan.bluetoothconnection.ui.home

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.Permission
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {


    private val _viewState = MutableLiveData<HomeViewState>().apply {
        value = HomeViewState(isLoading = true)
    }
    val viewState: LiveData<HomeViewState> = _viewState

    val requiredPermissions = Permission.Bluetooth

    fun findBluetoothDevices() {
        Log.d("HomeViewModel", "Find bluetooth devices")
    }

    fun permissionRequestDenied() {
        Log.d("HomeViewModel", "Permission Denied. We should show an error state")
    }
}

data class HomeViewState(
    val isLoading: Boolean = false,
    val shouldRequestPermissions: Boolean = false,
    val message: String? = null,
    val data: List<BluetoothDevice> = listOf(),
)
