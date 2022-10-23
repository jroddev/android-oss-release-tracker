package com.jroddev.android_oss_release_tracker.ui

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.volley.RequestQueue
import com.jroddev.android_oss_release_tracker.repo.MetaDataState
import com.jroddev.android_oss_release_tracker.repo.RepoMetaData


@Composable
fun TrackerPreview(
    repoUrl: String,
    requestQueue: RequestQueue,
    onAdd: (String) -> Unit
) {
    val metaData = remember { RepoMetaData(
        repoUrl,
        requestQueue
    ) }
    val isValid = remember {
        mutableStateOf(false)
    }

    if (!isValid.value && metaData.state.value == MetaDataState.Loaded) {
        isValid.value = true
    }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp, 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier = Modifier.size(50.dp, 50.dp).padding(10.dp, 0.dp),
                model = metaData.iconUrl,
                contentDescription = null
            )
            Column(modifier = Modifier.padding(10.dp, 0.dp)) {
                Text(text = metaData.appName)
                Text(text = metaData.packageName.value ?: "<loading>")
                Text(text = metaData.latestVersion.value ?: "<loading>")
                Text(text = metaData.latestVersionDate.value ?: "<loading>")
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Button(enabled = isValid.value , onClick = { onAdd(repoUrl) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }

}


@Composable
fun NewTrackerScreen(
    sharedPreferences: SharedPreferences,
    requestQueue: RequestQueue
) {
    val repoInputBox = remember { mutableStateOf("") }
    val isTested = remember { mutableStateOf(false) }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Add a new App Tracker",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(0.dp, 20.dp)
        )
        
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp),
            label = { Text(text = "Project Repository URL") },
            placeholder = { Text(text = "https://github.com/jroddev/android-oss-tracker") },
            value = repoInputBox.value,
            onValueChange = {
                repoInputBox.value = it.trim()
                isTested.value = false
            }
        )

        if (isTested.value) {
            TrackerPreview(repoInputBox.value, requestQueue) { repo -> println("Add $repo") }
        }


        Spacer(modifier = Modifier.weight(1.0f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = { isTested.value = true }) {
                Text(text = "Test")
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }


}