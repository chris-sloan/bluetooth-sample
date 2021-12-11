package com.chrissloan.bluetoothconnection

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.chrissloan.bluetoothconnection.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val bluetoothRequestId = 10001

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    Log.d("TT", "Found device : ${device?.name} - ${device?.type}, ${device?.address}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupBluetoothIfPossible()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    private fun setupBluetoothIfPossible() {
        checkPermission(
            permission = Manifest.permission.BLUETOOTH_CONNECT,
            requestCode = REQUEST_CODE_CONNECT,
            success = { checkBluetoothAvailability() }
        )
    }

    private fun checkPermission(permission: String, requestCode: Int, success: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED -> success.invoke()
            shouldShowRequestPermissionRationale(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
//                showInContextUI(...)
                Log.d("TT", "Show the why")
            }
            else -> requestPermissions(arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("TT", "onRequestPermissionsResult : $requestCode, $permissions, $grantResults")
    }

    private fun checkBluetoothAvailability() {
        bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

        Log.d("TT", "Checking for BT")
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH) }?.also {
            Toast.makeText(this, "Bluetooth Not Available", Toast.LENGTH_SHORT).show()
            finish()
        }
        Log.d("TT", "BT Done")
        Log.d("TT", "Checking for BLE")
        // Check to see if the BLE feature is available.
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
            ?.also {
                Toast.makeText(this, "Bluetooth LE Not Available", Toast.LENGTH_SHORT).show()
                finish()
            }
        Log.d("TT", "BLE Done")

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)


        val isEnabled = bluetoothAdapter?.isEnabled
        Log.d("TT", "Enabled is $isEnabled")

        if (isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
        }
        Log.d("TT", "Paired Devices : $pairedDevices")

        checkPermission(
            permission = Manifest.permission.BLUETOOTH_SCAN,
            requestCode = REQUEST_CODE_SCAN,
            success = { bluetoothAdapter?.startDiscovery() }
        )

    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes

            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("totalterrain",
                UUID.fromString("totalterrain"))
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e("totalterrain", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {

//                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e("totalterrain", "Could not close the connect socket", e)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_CONNECT = 10001
        private const val REQUEST_CODE_SCAN = 10002
    }

}

private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)


