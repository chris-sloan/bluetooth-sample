package com.chrissloan.bluetoothconnection

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.SystemPermissionsRequestHandler
import com.chrissloan.bluetoothconnection.ui.home.HomePage
import com.chrissloan.bluetoothconnection.ui.navigation.BottomNav
import com.chrissloan.bluetoothconnection.ui.navigation.Screen
import com.chrissloan.bluetoothconnection.ui.theme.BasicsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val systemPermissionsRequestHandler = SystemPermissionsRequestHandler.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(fabClickListener)
        }
    }

    private val fabClickListener = {
        systemPermissionsRequestHandler
            .request(viewModel.requiredPermissions)
            .rationale(getString(R.string.bluetooth_request_rationale))
            .checkDetailedPermission { wasGranted ->
                if (wasGranted.all { it.value }) {
                    viewModel.refreshBluetoothDevices()
                } else {
                    viewModel.permissionRequestDenied()
                }
            }
    }
}

@Composable
fun MainContent(fabClickListener: () -> Unit) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<MainViewModel>()
    val viewState = viewModel.viewState.observeAsState().value
    val navController = rememberNavController()
    Timber.d("View state is : $viewState")
    BasicsTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name)) },
                    backgroundColor = MaterialTheme.colors.primary,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { scaffoldState.drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Filled.Menu, "")
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.Refresh, "Find Bluetooth Devices") },
                    text = { Text("Find Devices") },
                    onClick = fabClickListener,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
            },
            drawerContent = { Text(text = "drawerContent") },
            bottomBar = { BottomNav(navController) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Devices.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Devices.name) {
                    Timber.i("Devices list size is : ${viewState?.data}")
                    HomePage(devices = viewState?.data ?: emptyList()) {
                        Timber.d("Homepage clicked")
                    }
                }
                composable(Screen.Files.name) {
                    Text("Files Upload")
                }
            }
        }
    }
}
