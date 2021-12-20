package com.chrissloan.bluetoothconnection.dependencies.android.broadcast

import android.app.Application
import android.content.BroadcastReceiver
import android.content.IntentFilter

class BroadcastRegistrar(
    private val application: Application
) {

    fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        application.registerReceiver(receiver, filter)
    }

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        application.unregisterReceiver(receiver)
    }
}
