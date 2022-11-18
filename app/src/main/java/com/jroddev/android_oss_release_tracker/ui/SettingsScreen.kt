package com.jroddev.android_oss_release_tracker.ui

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.jroddev.android_oss_release_tracker.PersistentState
import androidx.compose.ui.text.font.FontWeight
import com.jroddev.android_oss_release_tracker.util.FileHelpers


@Composable
fun RepoListImporter() {
    val ctx = LocalContext.current
    val sharedPreferences = ctx.getSharedPreferences(
        PersistentState.STATE_FILENAME,
        ComponentActivity.MODE_PRIVATE
    )
    val reader = FileHelpers.readFile({ data ->
        // read it
        println("Read file content: $data")
        if (data.isNotEmpty()) {
            PersistentState.addTrackers(ctx, sharedPreferences, data.lines())
        }
    }, {
        // error opening file
        Toast.makeText(ctx, "Could not import repo list", Toast.LENGTH_SHORT).show()
    })

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            reader.launch(arrayOf("text/plain"))
        },
    ) {
        Text(text = "Import Repo List")
    }
}

@Composable
fun RepoListExporter() {
    val ctx = LocalContext.current
    val sharedPreferences = ctx.getSharedPreferences(
        PersistentState.STATE_FILENAME,
        ComponentActivity.MODE_PRIVATE
    )
    val data = PersistentState.getSavedTrackers(sharedPreferences).joinToString("\n")
    val writer = FileHelpers.openWritableTextFile({ uri ->
        // write it
        FileHelpers.writeToFile(uri, data, ctx)
    }, {
        // error opening file
        Toast.makeText(ctx, "Could not export repo list", Toast.LENGTH_SHORT).show()
    })

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            writer.launch("oss_trackers.txt")
        }) {
        Text(text = "Export Repo List")
    }
}

@Composable
fun RepoDeleteAll() {
    val ctx = LocalContext.current
    val sharedPreferences = ctx.getSharedPreferences(
        PersistentState.STATE_FILENAME,
        ComponentActivity.MODE_PRIVATE
    )
    val showDeleteAllPopup = remember { mutableStateOf(false) }

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            showDeleteAllPopup.value = true
        }) {
        Text(text = "Delete all trackers")
    }

    if (showDeleteAllPopup.value) {

        Box(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()) {
            Popup(
                alignment = Alignment.TopCenter,

                ) {
                Card(
                    modifier = Modifier
                        .width(300.dp),
                    border = BorderStroke(2.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Delete all trackers?", fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(50.dp))
                        Row {
                            TextButton(
                                onClick = {
                                    PersistentState.removeAllTrackers(ctx, sharedPreferences)
                                    showDeleteAllPopup.value = false
                                }) {
                                Text(text = "Delete")
                            }
                            Spacer(modifier = Modifier.width(50.dp))
                            TextButton(
                                onClick = {
                                    showDeleteAllPopup.value = false
                                }) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun SettingsScreen() {

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 20.dp)
        )
        RepoListImporter()
        RepoListExporter()
        RepoDeleteAll()
    }
}

