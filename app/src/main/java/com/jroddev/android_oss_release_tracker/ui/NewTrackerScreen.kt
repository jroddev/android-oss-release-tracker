package com.jroddev.android_oss_release_tracker.ui

import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.volley.RequestQueue
import com.jroddev.android_oss_release_tracker.PersistentState
import com.jroddev.android_oss_release_tracker.repo.MetaDataState
import com.jroddev.android_oss_release_tracker.repo.RepoMetaData


@Composable
fun TrackerPreview(
    repoUrl: String,
    requestQueue: RequestQueue,
    onAdd: (String, String) -> Unit
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
    val fallbackText = when(metaData.state.value) {
        MetaDataState.Unsupported -> "<unsupported>"
        MetaDataState.Loading -> "<loading>"
        MetaDataState.Errored -> "<error>"
        MetaDataState.Loaded -> "<loaded but null>"
    }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp, 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (metaData.iconUrl.value != null) {
                AsyncImage(
                    modifier = Modifier.size(50.dp, 50.dp).padding(10.dp, 0.dp),
                    model = metaData.iconUrl.value,
                    contentDescription = null
                )
            }
            Column(modifier = Modifier.padding(10.dp, 0.dp)) {
                Text(text = metaData.appName)
                Text(text = metaData.packageName.value ?: fallbackText)
                Text(text = metaData.latestVersion.value ?: fallbackText)
                Text(text = metaData.latestVersionDate.value ?: fallbackText)
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Button(enabled = isValid.value , onClick = { onAdd(repoUrl, metaData.appName) }) {
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
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
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
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
        )

        if (isTested.value) {
            TrackerPreview(repoInputBox.value, requestQueue) { repo, appName ->
                run {
                    PersistentState.addTracker(ctx, sharedPreferences, appName, repo)
                    Toast.makeText(ctx, "Added $appName to your trackers", Toast.LENGTH_LONG).show()
                    repoInputBox.value = ""
                    isTested.value = false

                    println("trackers: ${sharedPreferences.getStringSet("app_trackers", setOf())!!}")
                }
            }
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