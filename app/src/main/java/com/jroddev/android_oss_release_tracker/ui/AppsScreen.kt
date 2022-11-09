package com.jroddev.android_oss_release_tracker.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
        val set =  mutableStateListOf<String>()
        set.addAll(PersistentState.getSavedTrackers(sharedPreferences))
        set
    }

    val onTrackerDelete = { appName: String, repo: String -> run {
        PersistentState.removeTracker(ctx, sharedPreferences, appName, repo)
        repoUrls.remove(repo)
        Unit
    }}

    Column(modifier = Modifier.verticalScroll(verticalScroll)) {
        Text(
            text = "Application Trackers",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(0.dp, 20.dp)
        )
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
        Row(modifier = Modifier.absolutePadding(right = 20.dp)) {
            if (metaData.iconUrl.value != null) {
                AsyncImage(
                    modifier = Modifier.size(50.dp, 50.dp),
                    model = metaData.iconUrl.value,
                    contentDescription = null
                )
            }
            Text(text = metaData.repoUrl)
        }
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (metaData.iconUrl.value != null) {
            AsyncImage(
                modifier = Modifier.size(50.dp, 50.dp),
                model = metaData.iconUrl.value,
                contentDescription = null
            )
        }
        Column(modifier = Modifier.padding(Dp(15f), Dp(0f))) {
            Text(text = metaData.appName)

            if (metaData.packageName.value == null) {
                Text(text = "installed: " + (metaData.installedVersion.value ?: "<loading>"))
            } else {
                Text(text = "installed: ${metaData.installedVersion.value}")
            }

            val newVersionAvailable = metaData.installedVersion.value == null || (metaData.latestVersion.value ?: "") > (metaData.installedVersion.value ?: "")
            Text(
                text = "latest: ${metaData.latestVersion.value ?: "<loading>"}${if (newVersionAvailable) " (NEW)" else ""}",
                fontWeight = if (newVersionAvailable) FontWeight.ExtraBold else FontWeight.Normal
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



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RenderItem(
    packageManager: PackageManager,
    requestQueue: RequestQueue,
    repoUrl: String,
    onDelete: (String, String) -> Unit
) {
    val ctx = LocalContext.current
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
            .padding(0.dp, 5.dp),
        onClick = {
            val link = metaData.latestVersionUrl.value ?: metaData.repoUrl
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(link)
            )
            ctx.startActivity(urlIntent)
        }
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