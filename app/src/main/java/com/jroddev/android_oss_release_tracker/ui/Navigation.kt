package com.jroddev.android_oss_release_tracker.ui

import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.volley.RequestQueue

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route:String,
)

object Constants {
    val BottomNavItems = listOf(
        BottomNavItem(
            label = "Apps",
            icon = Icons.Filled.Home,
            route = "apps"
        ),
        BottomNavItem(
            label = "New",
            icon = Icons.Filled.Add,
            route = "new"
        ),
        BottomNavItem(
            label = "Settings",
            icon = Icons.Filled.Settings,
            route = "settings"
        )
    )
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    padding: PaddingValues,
    sharedPreferences: SharedPreferences,
    packageManager: PackageManager,
    requestQueue: RequestQueue
) {

    NavHost(
        navController = navController,
        startDestination = "apps",
        modifier = Modifier.padding(paddingValues = padding),
        builder = {

            // route : Home
            composable("apps") {
                AppsScreen(packageManager, sharedPreferences, requestQueue)
            }

            // route : search
            composable("new") {
                NewTrackerScreen(sharedPreferences, requestQueue)
            }

            // route : profile
            composable("settings") {
                SettingsScreen()
            }
        })
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {

    BottomNavigation(backgroundColor = MaterialTheme.colors.background) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Constants.BottomNavItems.forEach { navItem ->
            BottomNavigationItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route)
                },
                icon = {
                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                },
                label = {
                    Text(text = navItem.label)
                },
                alwaysShowLabel = false
            )
        }
    }
}