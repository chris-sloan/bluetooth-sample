package com.chrissloan.bluetoothconnection.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.SystemPermissionsRequestHandler
import com.chrissloan.bluetoothconnection.ui.theme.BasicsCodelabTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val systemPermissionsRequestHandler = SystemPermissionsRequestHandler.from(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeContent(fabClickListener)
            }
        }
    }

    private val fabClickListener = {
        systemPermissionsRequestHandler
            .request(homeViewModel.requiredPermissions)
            .rationale(getString(R.string.bluetooth_request_rationale))
            .checkDetailedPermission { wasGranted ->
                if (wasGranted.all { it.value }) {
                    homeViewModel.findBluetoothDevices()
                } else {
                    homeViewModel.permissionRequestDenied()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.viewState.observe(viewLifecycleOwner, {
            Timber.d("viewState -> $it")
            when {
                it.isLoading -> {}
            }
        })
    }
}

@Composable
fun HomeContent(clickListener: () -> Unit) {
    BasicsCodelabTheme {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.Refresh, "") },
                    text = { Text("Find Devices") },
                    onClick = clickListener,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
            },
            content = {
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        )
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeContent {}
}
