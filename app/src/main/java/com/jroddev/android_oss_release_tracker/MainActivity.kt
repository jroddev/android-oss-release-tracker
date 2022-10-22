package com.jroddev.android_oss_release_tracker

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
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

                Text(text = "latest: ${latestVersion.value ?: "<loading>"}")
                Text(text = latestVersionDate.value ?: "", fontSize = 12.sp)
            }

            Column(
                modifier = Modifier.height(81.dp).padding(5.dp, 0.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Make this a hyperlink on the latest version text to save space
                if (latestVersionUrl.value != null) {
                    Button(onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(latestVersionUrl.value)
                        )
                        ctx.startActivity(urlIntent)
                    }) {
                        Text(text = "Latest")
                    }
                }
                val packageNameValue = packageName.value
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
