package com.jroddev.android_oss_release_tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
                latestVersion = "v1.0.1"
            ),
            ApplicationInfo(
                name = "Aurora Store",
                packageName = "unknown",
                rssUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/tags?format=atom",
                buildGradleUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/build.gradle",
                latestReleaseUrl = "https://gitlab.com/AuroraOSS/AuroraStore/-/tags/4.1.1",
                installedVersion = "not found",
                latestVersion = "v1.0.1"
            ),
            ApplicationInfo(
                name = "NewPipe",
                packageName = "unknown",
                rssUrl = "https://github.com/TeamNewPipe/NewPipe/releases.atom",
                buildGradleUrl = "https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle",
                latestReleaseUrl = "https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0",
                installedVersion = "not found",
                latestVersion = "v1.0.1"
            ),
            ApplicationInfo(
                name = "DavX5",
                packageName = "unknown",
                rssUrl = "https://github.com/bitfireAT/davx5-ose/releases.atom",
                buildGradleUrl = "https://raw.githubusercontent.com/bitfireAT/davx5-ose/dev-ose/app/build.gradle",
                latestReleaseUrl = "https://github.com/bitfireAT/davx5-ose/releases/tag/v4.2.3.4-ose",
                installedVersion = "not found",
                latestVersion = "v1.0.1"
            )
        )

        println("SEARCHING INSTALLED APPLICATIONS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
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

        setContent {
            AndroidossreleasetrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    MainPage(apps)
                }
            }
        }
    }
}

data class ApplicationInfo(
    val name: String,
    var packageName: String,
    val rssUrl: String,
    val buildGradleUrl: String,
    val latestReleaseUrl: String,
    var installedVersion: String,
    val latestVersion: String
)



@Composable
fun MainPage(apps: List< ApplicationInfo>) {
    val ctx = LocalContext.current

    Column {
        apps.forEach { app ->
            Card(modifier = Modifier.fillMaxWidth().padding(Dp(0f), Dp(5f))) {
                Row  (
                    modifier = Modifier.fillMaxWidth().padding(Dp(10f), Dp(5f)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom){
                    Column {
                        Text(text = app.packageName)
                        Text(text = app.name)
                        Text(text = "installed: " + app.installedVersion)
                        Text(text = "latest: " + app.latestVersion)

                    }
                    Button(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(app.latestReleaseUrl)
                        )
                        ctx.startActivity(urlIntent)
                    }) {
                        Text(text = "go to latest")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidossreleasetrackerTheme {
        MainPage(listOf())
    }
}