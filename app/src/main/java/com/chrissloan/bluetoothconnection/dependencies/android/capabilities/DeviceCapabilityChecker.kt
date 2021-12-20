package com.chrissloan.bluetoothconnection.dependencies.android.capabilities

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceCapabilityChecker @Inject constructor(
    @ApplicationContext context: Context
) {

    private val packageManager = context.packageManager

    fun hasDeviceCapability(capability: String) = packageManager.hasSystemFeature(capability)
}
