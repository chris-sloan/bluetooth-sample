package com.chrissloan.bluetoothconnection.dependencies.android.permissions

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.os.Build

sealed class Permission(vararg val permissions: String) {

    object Bluetooth : Permission(
        permissions =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            arrayOf(BLUETOOTH, ACCESS_FINE_LOCATION)
        } else {
            arrayOf(BLUETOOTH_CONNECT)
        }
    )

    object Location: Permission(ACCESS_FINE_LOCATION )

    companion object {
        fun from(permission: String) = when (permission) {
            BLUETOOTH,
            BLUETOOTH_CONNECT -> Bluetooth
            ACCESS_FINE_LOCATION -> Location
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}
