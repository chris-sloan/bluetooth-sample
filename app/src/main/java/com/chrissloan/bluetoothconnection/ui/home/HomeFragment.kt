package com.chrissloan.bluetoothconnection.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.ui.theme.BasicsTheme
import timber.log.Timber

@Composable
fun HomePage(
    devices: List<BluetoothDevice> = emptyList(),
    clickListener: (BluetoothDevice) -> Unit
) {
    Timber.i("Incoming devicess : $devices")
    BasicsTheme {
        HomeContent(
            devices = devices,
            clickListener
        )
    }
}

@Composable
fun HomeContent(
    devices: List<BluetoothDevice>,
    clickListener: (BluetoothDevice) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items = devices) { device ->
            Card(device = device,
            clickListener = clickListener)
        }
    }

}

@Composable
fun Card(
    device: BluetoothDevice,
    clickListener: (BluetoothDevice) -> Unit
) {
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { clickListener(device) }
    ) {
        CardContent(device)
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun CardContent(device: BluetoothDevice) {
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
            Text(text = device.address)
            Text(
                text = device.name ?: "Unknown device",
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
