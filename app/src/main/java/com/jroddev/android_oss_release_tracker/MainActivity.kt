package com.jroddev.android_oss_release_tracker

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.jroddev.android_oss_release_tracker.repo.MetaDataState
import com.jroddev.android_oss_release_tracker.repo.RepoMetaData
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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    RenderList(
                        packageManager,
                        requestQueue,
                        listOf(
                            "https://github.com/jroddev/android-oss-release-tracker",
//                            "https://gitlab.com/AuroraOSS/AuroraStore",
                            "https://github.com/TeamNewPipe/NewPipe",
                            "https://github.com/bitfireAT/davx5-ose"
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RenderList(
    packageManager: PackageManager,
    requestQueue: RequestQueue,
    repoUrls: List<String>
) {
    Column {
        repoUrls.forEach { url -> RenderItem(packageManager, requestQueue, url) }
    }
}


@Composable
fun UnsupportedTracker(metaData: RepoMetaData) {
    Text(text = "${metaData.repoUrl} could not be parsed or is not supported")
}

@Composable
fun LoadingTracker(metaData: RepoMetaData) {
    Text(text = "loading ${metaData.appName}")
}

@Composable
fun ErroredTracker(metaData: RepoMetaData) {
    // Show what we can, popup scrollable of errors

    val showErrors = remember { mutableStateOf(false) }
    val bullet = "\u2022"

    Column {
        Text(text = metaData.repoUrl)
        Button(onClick = { showErrors.value = !showErrors.value }) {
            Text(text = if (showErrors.value) "Hide Errors" else "Show Errors")
        }
        if (showErrors.value) {
            Column(modifier = Modifier.padding(Dp(15f), Dp(0f))) {
                metaData.errors.forEach {
                    Text(text = "$bullet $it", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun LoadedTracker(metaData: RepoMetaData) {
    // Show the full card
    val ctx = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.size(50.dp, 50.dp)) {
            println("load ${metaData.iconUrl}")
            AsyncImage(
                model = metaData.iconUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.padding(Dp(15f), Dp(0f))) {
            Text(text = metaData.appName)

            if (metaData.packageName.value == null) {
                Text(text = "installed: " + (metaData.installedVersion.value ?: "<loading>"))
            } else {
                Text(text = "installed: ${metaData.installedVersion.value}")
            }

            val latestInstalled = metaData.latestVersion.value != null
                    && metaData.installedVersion.value != null
                    && metaData.latestVersion.value!! <= metaData.installedVersion.value!!
            Text(
                text = "latest: ${metaData.latestVersion.value ?: "<loading>"}",
                color = if(!latestInstalled) Color.Blue else Color.Black
            )
            Text(text = metaData.latestVersionDate.value ?: "", fontSize = 12.sp)
        }

        Column(
            modifier = Modifier
                .height(81.dp)
                .padding(5.dp, 0.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Make this a hyperlink on the latest version text to save space
            if (metaData.latestVersionUrl.value != null) {
                Button(onClick = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(metaData.latestVersionUrl.value)
                    )
                    ctx.startActivity(urlIntent)
                }) {
                    Text(text = "Latest")
                }
            }
            val packageNameValue = metaData.packageName.value
            if (packageNameValue != null) {
                Text(
                    text = packageNameValue,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp
                )
            }

        }

    }


}



@Composable
fun RenderItem(
    packageManager: PackageManager,
    requestQueue: RequestQueue,
    repoUrl: String
) {
    val state = remember { mutableStateOf(MetaDataState.Loading) }
    val packageName = remember { mutableStateOf<String?>(null) }
    val installedVersion = remember { mutableStateOf<String?>(null) }
    val latestVersionDate = remember { mutableStateOf<String?>(null) }
    val latestVersion = remember { mutableStateOf<String?>(null) }
    val latestVersionUrl = remember { mutableStateOf<String?>(null) }
    val errors = remember { mutableStateListOf<String>() }

    val metaData = RepoMetaData(
        requestQueue,
        state,
        repoUrl,
        packageName,
        installedVersion,
        latestVersion,
        latestVersionDate,
        latestVersionUrl,
        errors
    )

    if (metaData.installedVersion.value == null && metaData.packageName.value != null) {
        metaData.installedVersion.value = packageManager
            .getInstalledPackages(0)
            .find { it.packageName == packageName.value }
            ?.versionName ?: "not installed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
    ) {
        when(metaData.state.value) {
            MetaDataState.Unsupported -> UnsupportedTracker(metaData)
            MetaDataState.Loading -> LoadingTracker(metaData)
            MetaDataState.Errored -> ErroredTracker(metaData)
            MetaDataState.Loaded -> LoadedTracker(metaData)
        }
    }
}