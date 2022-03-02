package com.chrissloan.bluetoothconnection.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.chrissloan.bluetoothconnection.ui.home.HomePage
import timber.log.Timber

enum class Screen(
    val icon: ImageVector,
    val body: @Composable ((String) -> Unit) -> Unit
) {
    Devices(
        icon = Icons.Filled.Devices,
        body = { HomePage {} }
    ),
    Files(
        icon = Icons.Filled.FileUpload,
        body = { Timber.d("Files Page 2") }
    );

    @Composable
    fun Content(onScreenChange: (String) -> Unit) {
        body(onScreenChange)
    }

    companion object {
        fun fromRoute(route: String?): Screen =
            when (route?.substringBefore("/")) {
                Devices.name -> Devices
                Files.name -> Files
                null -> Devices
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}
