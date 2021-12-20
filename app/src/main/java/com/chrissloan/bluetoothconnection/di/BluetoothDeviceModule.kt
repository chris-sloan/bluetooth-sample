package com.chrissloan.bluetoothconnection.di

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class BluetoothDeviceModule {

    @Provides
    fun provideBluetoothManager(
        @ApplicationContext context: Context
    ): BluetoothManager =
        context.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
}
