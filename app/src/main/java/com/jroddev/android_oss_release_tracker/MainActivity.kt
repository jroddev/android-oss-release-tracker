package com.jroddev.android_oss_release_tracker

import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.jroddev.android_oss_release_tracker.repo.GitHub
import com.jroddev.android_oss_release_tracker.repo.Repo
import com.jroddev.android_oss_release_tracker.repo.RepoMetaData
import com.jroddev.android_oss_release_tracker.ui.theme.AndroidossreleasetrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = listOf(
            ApplicationInfo(
                name = "Android OSS Release Tracker",
                packageName = "unknown",
                rssUrl = "",
                buildGradleUrl = "",
                latestReleaseUrl = "",
                installedVersion = "not found",
                latestVersion = "v1.0.1",
                iconUrl = null
            ),
            ApplicationInfo(
                name = "Aurora Store",
                packageName = "unknown",
                rssUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/tags?format=atom",
                buildGradleUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/build.gradle",
                latestReleaseUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/tags/4.1.1",
                installedVersion = "not found",
                latestVersion = "v1.0.1",
                iconUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png"
            ),
            ApplicationInfo(
                name = "NewPipe",
                packageName = "unknown",
                rssUrl = "https://github.com/TeamNewPipe/NewPipe/releases.atom",
                buildGradleUrl = "https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle",
                latestReleaseUrl = "https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0",
                installedVersion = "not found",
                latestVersion = "v1.0.1",
                iconUrl = "https://github.com/TeamNewPipe/NewPipe/raw/dev/app/src/main/res/mipmap-mdpi/ic_launcher.png"
            ),
            ApplicationInfo(
                name = "DavX5",
                packageName = "unknown",
                rssUrl = "https://github.com/bitfireAT/davx5-ose/releases.atom",
                buildGradleUrl = "https://raw.githubusercontent.com/bitfireAT/davx5-ose/dev-ose/app/build.gradle",
                latestReleaseUrl = "https://github.com/bitfireAT/davx5-ose/releases/tag/v4.2.3.4-ose",
                installedVersion = "not found",
                latestVersion = "v1.0.1",
                iconUrl = "https://github.com/bitfireAT/davx5-ose/raw/dev-ose/app/src/main/res/mipmap-mdpi/ic_launcher.png"
            )
        )

        println("SEARCHING INSTALLED APPLICATIONS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        // TODO: I have the names of packages I'm tracking. Request only them?
        val installedPackages = packageManager.getInstalledPackages(0)
        println(installedPackages)
        installedPackages.forEach { it ->
            if (it.packageName == "com.jroddev.android_oss_release_tracker") {
                println("found: ${it} (${it.versionName})")
                apps[0].packageName = it.packageName
                apps[0].installedVersion = it.versionName
            }
        }

        installedPackages.forEach { it ->
            if (it.packageName.lowercase().contains("aurora")) {
                println("found: ${it} (${it.versionName})")
                apps[1].packageName = it.packageName
                apps[1].installedVersion = it.versionName
            }
        }

        installedPackages.forEach { it ->
            if (it.packageName.lowercase().contains("pipe")) {
                println("found: ${it} (${it.versionName})")
                apps[2].packageName = it.packageName
                apps[2].installedVersion = it.versionName
            }
        }

        installedPackages.forEach { it ->
            if (it.packageName.lowercase().contains("dav")) {
                println("found: ${it} (${it.versionName})")
                apps[3].packageName = it.packageName
                apps[3].installedVersion = it.versionName
            }
        }


        println("FINISHED SEARCHING INSTALLED APPLICATIONS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

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
                            "https://giithub.com/jroddev/android-oss-release-tracker",
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

data class ApplicationInfo(
    val name: String,
    var packageName: String,
    val rssUrl: String, // not needed here
    val buildGradleUrl: String, // not needed here
    val latestReleaseUrl: String,
    var installedVersion: String,
    val latestVersion: String,
    var iconUrl: String?
)

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
fun RenderItem(
    packageManager: PackageManager,
    requestQueue: RequestQueue,
    repoUrl: String
) {
    val ctx = LocalContext.current

    val packageName = remember { mutableStateOf<String?>(null) }
    val installedVersion = remember { mutableStateOf<String?>(null) }
    val latestVersionDate = remember { mutableStateOf<String?>(null) }
    val latestVersion = remember { mutableStateOf<String?>(null) }
    val latestVersionUrl = remember { mutableStateOf<String?>(null) }
    val errors = remember { mutableStateListOf<String>() }

    val metaData = RepoMetaData(
        requestQueue,
        repoUrl,
        packageName,
        latestVersion,
        latestVersionDate,
        latestVersionUrl,
        errors
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(0f), Dp(5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dp(10f), Dp(5f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.size(Dp(50f), Dp(50f))) {
                    println("load ${metaData.iconUrl}")
                    AsyncImage(
                        model = metaData.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.padding(Dp(15f), Dp(0f))) {
                    Text(text = metaData.appName)

                    if (packageName.value == null) {
                        Text(text = "installed: " + (installedVersion.value ?: "<loading>"))
                    } else {
                        if (installedVersion.value == null) {
                            installedVersion.value = packageManager
                                .getInstalledPackages(0)
                                .find { it.packageName == packageName.value }
                                ?.versionName ?: "not installed"
                        }
                        Text(text = "installed: ${installedVersion.value}")
                    }
                    if (latestVersion.value.isNullOrEmpty()) {
                        Text(text = "latest: <loading>")
                    } else {
                        Text(text = "latest: " + latestVersion.value)
                    }
                }
            }


            val packageNameValue = packageName.value
            if (packageNameValue != null) {
                Text(text = packageNameValue)
            }
        }

    }
    // Make this a hyperlink on the latest version text to save space
    if (latestVersionUrl.value != null) {
        Button(onClick = {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(latestVersionUrl.value)
            )
            ctx.startActivity(urlIntent)
        }) {
            Text(text = "Go")
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    AndroidossreleasetrackerTheme {
//        RenderList(
//                        packageManager,
//                        requestQueue,
//                        listOf(
//                            "https://giithub.com/jroddev/android-oss-release-tracker",
////                            "https://gitlab.com/AuroraOSS/AuroraStore",
//                            "https://github.com/TeamNewPipe/NewPipe",
//                            "https://github.com/bitfireAT/davx5-ose"
//                        )
//                    )
//    }
//}
