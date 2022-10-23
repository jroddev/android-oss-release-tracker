package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.android.volley.RequestQueue

enum class MetaDataState {
    Unsupported,
    Loading,
    Errored,
    Loaded
}


data class RepoMetaData(
    val repoUrl: String,
    val requestQueue: RequestQueue,
    ) {
    private val repo: Repo? = Repo.Helper.new(repoUrl)
    var orgName: String
    var appName: String
    var iconUrl: String
    val state = mutableStateOf(MetaDataState.Unsupported)
    val packageName = mutableStateOf<String?>(null)
    val installedVersion = mutableStateOf<String?>(null)
    val latestVersion = mutableStateOf<String?>(null)
    val latestVersionDate = mutableStateOf<String?>(null)
    val latestVersionUrl = mutableStateOf<String?>(null)
    val errors = mutableStateListOf<String>()

    init {
        if (repo == null) {
            state.value = MetaDataState.Unsupported
            orgName = ""
            appName = ""
            iconUrl = ""
        } else {
            state.value = MetaDataState.Loading
            orgName = repo.getOrgName(repoUrl)
            appName = repo.getApplicationName(repoUrl)
            iconUrl = repo.fetchIconUrl(repoUrl)
            repo.fetchPackageName(this, requestQueue)
            repo.fetchLatestVersion(this, requestQueue)
        }
    }
}


interface Repo {
    fun getOrgName(repoUrl: String): String
    fun getApplicationName(repoUrl: String): String
    fun fetchIconUrl(repoUrl: String): String
    fun fetchPackageName(metaData: RepoMetaData, requestQueue: RequestQueue)
    fun fetchLatestVersion(metaData: RepoMetaData, requestQueue: RequestQueue)

    object Helper {
        fun new(repoUrl: String): Repo? =
            if (repoUrl.contains("github")) {
                GitHub()
//        } else if (repoUrl.contains("gitlab")) {
//            GitLab(repoUrl)
            } else {
                null
            }
    }
}
