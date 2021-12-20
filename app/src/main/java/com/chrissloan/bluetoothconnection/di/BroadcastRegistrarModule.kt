package com.chrissloan.bluetoothconnection.di

import android.app.Application
import com.chrissloan.bluetoothconnection.dependencies.android.broadcast.BroadcastRegistrar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class BroadcastRegistrarModule {

    @Provides
    fun provideBluetoothManager(
        activity: Application
    ): BroadcastRegistrar = BroadcastRegistrar(activity)

}
