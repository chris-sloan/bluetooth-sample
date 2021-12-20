package com.chrissloan.bluetoothconnection.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.SystemPermissionsRequestHandler
import com.chrissloan.bluetoothconnection.ui.theme.BasicsTheme
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
                HomePage(fabClickListener)
            }
        }
    }

    private val fabClickListener = {
        systemPermissionsRequestHandler
            .request(homeViewModel.requiredPermissions)
            .rationale(getString(R.string.bluetooth_request_rationale))
            .checkDetailedPermission { wasGranted ->
                if ( wasGranted.all { it.value } ) {
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
                it.data.isNotEmpty() -> {
                    Timber.d("List is :${it.data}")
                }
            }
        })
    }
}

@Composable
fun HomePage(clickListener: () -> Unit) {
    BasicsTheme {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    icon = { Icon(Filled.Refresh, "Find Bluetooth Devices") },
                    text = { Text("Find Devices") },
                    onClick = clickListener,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
            },
            content = {
                HomeContent()
            }
        )
    }
}

@Composable
fun HomeContent(names: List<String> = List(100) { "$it" }) {
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }

}

@Composable
fun Greeting(name: String) {
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        CardContent(name)
    }
}

@Composable
private fun CardContent(name: String) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Text(text = "Hello, ")
            Text(
                text = name,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (expanded) {
                Text(
                    text = ("Composem ipsum color sit lazy, " +
                            "padding theme elit, sed do bouncy. ").repeat(4),
                )
            }
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Filled.ExpandLess else Filled.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.show_less)
                } else {
                    stringResource(R.string.show_more)
                }

            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomePage {}
}
