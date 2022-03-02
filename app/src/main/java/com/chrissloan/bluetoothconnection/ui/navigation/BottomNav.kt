package com.chrissloan.bluetoothconnection.ui.navigation

import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNav(navController: NavController) {
    val items = listOf(
        Screen.Devices,
        Screen.Files
    )
    androidx.compose.material.BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.name) },
                label = { Text(text = item.name, style = MaterialTheme.typography.caption) },
                selectedContentColor = MaterialTheme.colors.onSecondary,
                unselectedContentColor = MaterialTheme.colors.secondary,
                alwaysShowLabel = true,
                selected = currentRoute == item.name,
                onClick = {
                    navController.navigate(item.name) {
                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
