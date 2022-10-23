package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.volley.RequestQueue

enum class MetaDataState {
    Unsupported,
    Loading,
    Errored,
    Loaded
}

data class RepoMetaData(
    val requestQueue: RequestQueue,
    val state: MutableState<MetaDataState>,
    val repoUrl: String,
    val packageName: MutableState<String?>,
    val installedVersion: MutableState<String?>,
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
            GitHub(this, requestQueue)
//        } else if (repoUrl.contains("gitlab")) {
//            GitLab(repoUrl, requestQueue)
        } else {
            null
        }

        if (repo == null) {
            state.value = MetaDataState.Unsupported
            orgName = ""
            appName = ""
            iconUrl = ""
        }
        else {
            state.value = MetaDataState.Loading
            orgName = repo.getOrgName()
            appName = repo.getApplicationName()
            iconUrl = repo.fetchIconUrl()
            repo.fetchPackageName()
            repo.fetchLatestVersion()
        }
    }
}


interface Repo {
    fun getOrgName(): String
    fun getApplicationName(): String
    fun fetchIconUrl(): String
    fun fetchPackageName()
    fun fetchLatestVersion()
}
