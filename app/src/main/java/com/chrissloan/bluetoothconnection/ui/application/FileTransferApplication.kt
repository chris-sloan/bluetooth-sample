package com.chrissloan.bluetoothconnection.ui.application

import android.app.Application
import com.chrissloan.bluetoothconnection.BuildConfig
import com.chrissloan.bluetoothconnection.dependencies.android.timber.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree


@HiltAndroidApp
class FileTransferApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}
