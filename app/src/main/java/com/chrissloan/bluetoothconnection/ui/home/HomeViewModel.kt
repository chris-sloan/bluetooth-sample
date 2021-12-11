package com.chrissloan.bluetoothconnection.ui.home

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableLiveData<HomeViewState>().apply {
        value = HomeViewState(isLoading = true)
    }
    val viewState: LiveData<HomeViewState> = _viewState
}

data class HomeViewState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val data: List<BluetoothDevice> = listOf()
)
