package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.volley.RequestQueue

data class RepoMetaData(
    val requestQueue: RequestQueue,
    val repoUrl: String,
    val packageName: MutableState<String?>,
    val latestVersion: MutableState<String?>,
    val latestVersionDate: MutableState<String?>,
    val latestVersionUrl: MutableState<String?>,
    val errors: SnapshotStateList<String>,

    ) {
    lateinit var orgName: String
    lateinit var appName: String
    lateinit var iconUrl: String

    init {
        val repo: Repo? = if (repoUrl.contains("github")) {
            GitHub(repoUrl, requestQueue)
//        } else if (repoUrl.contains("gitlab")) {
//            GitLab(repoUrl, requestQueue)
        } else {
            orgName = ""
            appName = ""
            iconUrl = ""
            errors.add("Repo URL: $repoUrl could not be parsed or is not supported")
            null
        }

        if (repo != null) {
            orgName = repo.getOrgName()
            appName = repo.getApplicationName()
            iconUrl = repo.getIconUrl()
            repo.getPackageName(packageName, errors)
            repo.getLatestVersion(
                latestVersion,
                latestVersionDate,
                latestVersionUrl,
                errors
            )
        }
    }
}


interface Repo {
    fun getOrgName(): String
    fun getApplicationName(): String
    fun getIconUrl(): String
    fun getPackageName(output: MutableState<String?>, errors: SnapshotStateList<String>)
    fun getLatestVersion(version: MutableState<String?>, lastUpdated: MutableState<String?>,link: MutableState<String?>, errors: SnapshotStateList<String>)
}
