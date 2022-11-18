package com.jroddev.android_oss_release_tracker.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toFile
import java.io.*

object FileHelpers {
    @Composable
    fun openWritableTextFile(
        onSuccess: (Uri) -> Unit,
        onFailure: () -> Unit
    ): ManagedActivityResultLauncher<String, Uri?> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/plain"),
            onResult = { uri ->
                println("User selected URI: $uri to write to")
                try {
                    onSuccess(uri!!)
                } catch (e: Exception) {
                    println("Exception thrown trying to write file $uri. Exception: $e")
                    onFailure()
                }
            }
        )
    }

    @Composable
    fun readFile(
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ): ManagedActivityResultLauncher<Array<String>, Uri?> {
        val ctx = LocalContext.current

        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                println("User selected URI: $uri to write to")
                try {
                    onSuccess(readFile(uri!!, ctx))
                } catch (e: Exception) {
                    println("Exception thrown trying to read file $uri. Exception: $e")
                    onFailure()
                }
            }
        )
    }


    fun writeToFile(path: Uri, data: String, ctx: Context) {
        println("Writing to $path")
        try {
            val fos = if (path.toString().startsWith("file")) {
                FileOutputStream(path.toFile())
            } else {
                (ctx as Activity).contentResolver.openOutputStream(path)
            }
            if (fos != null) {
                fos.write(data.toByteArray())
                fos.close()
                Toast.makeText(ctx, "Repo list saved to $path", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(ctx, "Could not open file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(ctx, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }

    fun readFile(path: Uri, ctx: Context): String {
        println("Reading from $path")
        try {
            val fIn: InputStream? = if (path.toString().startsWith("file")) {
                FileInputStream(path.toFile())
            } else {
                (ctx as Activity).contentResolver.openInputStream(path)
            }
            if (fIn != null) {
                val isr = InputStreamReader(fIn)
                val buffreader = BufferedReader(isr)
                val datax = StringBuffer("")

                var readString: String? = buffreader.readLine()
                while (readString != null) {
                    if (readString.trim().isNotEmpty()) {
                        datax.append(readString).append("\n")
                        println("repo: $readString")
                    }
                    readString = buffreader.readLine()
                }
                return datax.toString().trim()
            } else {
                Toast.makeText(ctx, "Could not open file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(ctx, "Error saving file", Toast.LENGTH_SHORT).show()
        }
        return ""
    }
}