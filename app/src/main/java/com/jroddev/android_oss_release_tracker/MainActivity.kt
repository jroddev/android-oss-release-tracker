package com.jroddev.android_oss_release_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.jroddev.android_oss_release_tracker.ui.BottomNavigationBar
import com.jroddev.android_oss_release_tracker.ui.NavHostContainer
import com.jroddev.android_oss_release_tracker.ui.theme.AndroidossreleasetrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cache = DiskBasedCache(cacheDir, 1024 * 1024)
        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        setContent {
            AndroidossreleasetrackerTheme {

                val navController = rememberNavController()
                val sharedPreferences = getSharedPreferences(PersistentState.STATE_FILENAME, MODE_PRIVATE)

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(
                        bottomBar = { BottomNavigationBar(navController = navController) },
                        content = { padding ->
                            NavHostContainer(
                                navController = navController,
                                padding = padding,
                                sharedPreferences,
                                requestQueue
                            )
                        }
                    )
                }
            }
        }
    }
}

