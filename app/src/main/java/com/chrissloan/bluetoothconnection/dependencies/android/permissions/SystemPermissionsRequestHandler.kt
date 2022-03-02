package com.chrissloan.bluetoothconnection.dependencies.android.permissions

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chrissloan.bluetoothconnection.R
import java.lang.ref.WeakReference

class SystemPermissionsRequestHandler(private val activity: WeakReference<AppCompatActivity>) {

    private val requiredPermissions = mutableListOf<Permission>()
    private var rationale: String? = null
    private var callback: (Boolean) -> Unit = {}
    private var detailedCallback: (Map<Permission,Boolean>) -> Unit = {}

    private val permissionCheck =
        activity.get()?.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
            sendResultAndCleanUp(grantResults)
        }

    companion object {
        fun from(activity: AppCompatActivity) = SystemPermissionsRequestHandler(WeakReference(activity))
    }

    fun rationale(description: String): SystemPermissionsRequestHandler {
        rationale = description
        return this
    }

    fun request(vararg permission: Permission): SystemPermissionsRequestHandler {
        requiredPermissions.addAll(permission)
        return this
    }

    fun checkPermission(callback: (Boolean) -> Unit) {
        this.callback = callback
        handlePermissionRequest()
    }

    fun checkDetailedPermission(callback: (Map<Permission,Boolean>) -> Unit) {
        this.detailedCallback = callback
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() {
        activity.get()?.let { activity ->
            when {
                areAllPermissionsGranted(activity) -> sendPositiveResult()
                shouldShowPermissionRationale(activity) -> displayRationale(activity)
                else -> requestPermissions()
            }
        }
    }

    private fun displayRationale(activity: AppCompatActivity) {
        android.app.AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_permission_title))
            .setMessage(rationale ?: activity.getString(R.string.dialog_permission_default_message))
            .setCancelable(false)
            .setPositiveButton(activity.getString(R.string.dialog_permission_button_positive)) { _, _ ->
                requestPermissions()
            }
            .show()
    }

    private fun sendPositiveResult() {
        sendResultAndCleanUp(getPermissionList().associate { it to true } )
    }

    private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
        callback(grantResults.all { it.value })
        detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
        cleanUp()
    }

    private fun cleanUp() {
        requiredPermissions.clear()
        rationale = null
        callback = {}
        detailedCallback = {}
    }

    private fun requestPermissions() {
        permissionCheck?.launch(getPermissionList())
    }

    private fun areAllPermissionsGranted(activity: AppCompatActivity) =
        requiredPermissions.all { it.isGranted(activity) }

    private fun shouldShowPermissionRationale(activity: AppCompatActivity) =
        requiredPermissions.any { it.requiresRationale(activity) }

    private fun getPermissionList() =
        requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    private fun Permission.isGranted(activity: AppCompatActivity) =
        permissions.all { hasPermission(activity, it) }

    private fun Permission.requiresRationale(activity: AppCompatActivity) =
        permissions.any { activity.shouldShowRequestPermissionRationale(it) }

    private fun hasPermission(activity: AppCompatActivity, permission: String) =
        ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
