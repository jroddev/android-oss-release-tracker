package com.jroddev.android_oss_release_tracker.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jroddev.android_oss_release_tracker.PersistentState
import com.jroddev.android_oss_release_tracker.repo.MetaDataState
import com.jroddev.android_oss_release_tracker.repo.RepoMetaData


@Composable
fun AppsScreen(
    packageManager: PackageManager,
    sharedPreferences: SharedPreferences,
    requestQueue: RequestQueue
) {
    val ctx = LocalContext.current
    val verticalScroll = rememberScrollState()
    val repoUrls = remember {
        PersistentState.getSavedTrackers(sharedPreferences)
    }

    val onTrackerDelete = { appName: String, repo: String -> run {
        PersistentState.removeTracker(ctx, sharedPreferences, appName, repo)
        // TODO: Refresh this page
    }}

    Column(modifier = Modifier.verticalScroll(verticalScroll)) {
        if (repoUrls.isEmpty()) {
            Text(text = "You aren't tracking any application repositories")
        }
        repoUrls.forEach { url -> run {
            RenderItem(
                packageManager,
                requestQueue,
                url,
                onTrackerDelete)
        } }
    }
}


@Composable
fun UnsupportedTracker(metaData: RepoMetaData) {
    Text(text = "${metaData.repoUrl} could not be parsed or is not supported")
}

@Composable
fun LoadingTracker(metaData: RepoMetaData) {
    Row {
        Spacer(modifier = Modifier.size(50.dp, 50.dp))
        Text(text = "loading ${metaData.appName}")
    }
}

@Composable
fun ErroredTracker(metaData: RepoMetaData) {
    val showErrors = remember { mutableStateOf(false) }
    val bullet = "\u2022"

    Column(modifier = Modifier.padding(10.dp, 10.dp)) {
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
fun LoadedTracker(
    metaData: RepoMetaData
) {
    // TODO: Add something more obvious that says "Update Available
    val ctx = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            modifier = Modifier.size(50.dp, 50.dp),
            model = metaData.iconUrl,
            contentDescription = null
        )
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
                // TODO: This is not visible on Dark Mode
                color = if(!latestInstalled) Color.Blue else Color.Black
            )
            Text(text = metaData.latestVersionDate.value ?: "", fontSize = 12.sp)
        }

        Column(
            modifier = Modifier
                .height(100.dp)
                .padding(5.dp, 0.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            // Make this a hyperlink on the latest version text to save space
            if (metaData.latestVersionUrl.value != null) {
                Button(
                    modifier = Modifier.size(50.dp, 50.dp),
                    shape = RoundedCornerShape(50.dp),
                    onClick = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(metaData.latestVersionUrl.value)
                    )
                    ctx.startActivity(urlIntent)
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
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
    repoUrl: String,
    onDelete: (String, String) -> Unit
) {
    val metaData = remember { RepoMetaData(
        repoUrl,
        requestQueue
    ) }

    if (metaData.installedVersion.value == null && metaData.packageName.value != null) {
        metaData.installedVersion.value = packageManager
            .getInstalledPackages(0)
            .find { it.packageName == metaData.packageName.value }
            ?.versionName ?: "not installed"
    }

    Card(
        modifier = Modifier
            .defaultMinSize(0.dp, 90.dp)
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
    ) {

        Row {
            when (metaData.state.value) {
                MetaDataState.Unsupported -> UnsupportedTracker(metaData)
                MetaDataState.Loading -> LoadingTracker(metaData)
                MetaDataState.Errored -> ErroredTracker(metaData)
                MetaDataState.Loaded -> LoadedTracker(metaData)
            }
        }
        Row(
            modifier = Modifier.defaultMinSize(0.dp, 90.dp).padding(10.dp, 0.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top) {
            TextButton(
                onClick = { onDelete(metaData.appName, metaData.repoUrl) }) {
                Icon(Icons.Default.Delete, modifier = Modifier.size(40.dp, 40.dp), contentDescription = null)
            }
        }
    }
}