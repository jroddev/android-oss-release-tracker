package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import java.net.URL

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
            iconUrl = repo.getIconUrl(repoUrl)
            repo.fetchPackageName(this, requestQueue)
            repo.fetchLatestVersion(this, requestQueue)
        }
    }
}


interface Repo {
    fun getOrgName(repoUrl: String): String
    fun getApplicationName(repoUrl: String): String
    fun getIconUrl(repoUrl: String): String
    fun fetchPackageName(metaData: RepoMetaData, requestQueue: RequestQueue)
    fun fetchLatestVersion(metaData: RepoMetaData, requestQueue: RequestQueue)

    object Helper {
        fun new(repoUrl: String): Repo? =
            if (repoUrl.contains("github")) {
                GitHub()
        } else if (repoUrl.contains("gitlab")) {
                GitLab()
            } else {
                null
            }
    }
}


abstract class CommonRepo: Repo {
    abstract fun getBuildGradleUrl(org: String, app: String): String
    abstract fun getReadmeUrl(org: String, app: String): String
    abstract fun getRssFeedUrl(org: String, app: String): String

    // from repo URL https://gitlab.com/AuroraOSS/AuroraStore
    // returns AuroraOSS
    override fun getOrgName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[1]
    }

    // from repo URL https://gitlab.com/AuroraOSS/AuroraStore
    // returns AuroraStore
    override fun getApplicationName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[2]
    }

    override fun fetchPackageName(metaData: RepoMetaData, requestQueue: RequestQueue) {
        val url = getBuildGradleUrl(getOrgName(metaData.repoUrl), getApplicationName(metaData.repoUrl))
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val appId = RepoHelpers.parsePackageNameFromBuildGradle(response)
                    println("APP ID: $appId")
                    metaData.packageName.value = appId
                    if (RepoHelpers.isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value = MetaDataState.Loaded
                }  catch (e: Exception) {
                    println(e)
                    metaData.errors.add(e.localizedMessage ?: e.message ?: "Error occurred parsing $url")
                    metaData.state.value = MetaDataState.Errored
                }
            },
            { error ->
                println(error)
                metaData.errors.add(error.localizedMessage ?: error.message ?: "Error occurred fetching $url")
                // Add the error to the list but don't fail yet. Try fallback option
                fallbackPackageName(metaData, requestQueue)
            })
        requestQueue.add(request)
    }

    private fun fallbackPackageName(metaData: RepoMetaData, requestQueue: RequestQueue) {
        // Many READMEs in these repositories have links to F-droid and those urls contain the package name
        val url = getReadmeUrl(getOrgName(metaData.repoUrl), getApplicationName(metaData.repoUrl))
        println("running fallbackPackageName. url: $url")
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val appId = RepoHelpers.parsePackageNameFromREADME(response)
                    println("APP ID: $appId")

                    metaData.packageName.value = appId
                    if (RepoHelpers.isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value =
                        MetaDataState.Loaded
                }  catch (e: Exception) {
                    println(e)
                    metaData.errors.add(e.localizedMessage ?: e.message ?: "Error occurred parsing $url")
                    metaData.state.value = MetaDataState.Errored
                }
            },
            { error ->
                println(error)
                metaData.errors.add(error.localizedMessage ?: error.message ?: "Error occurred fetching $url")
                metaData.state.value = MetaDataState.Errored
            })
        requestQueue.add(request)
    }

    override fun fetchLatestVersion(metaData: RepoMetaData, requestQueue: RequestQueue) {
        val rss = getRssFeedUrl(getOrgName(metaData.repoUrl), getApplicationName(metaData.repoUrl))
        println("getLatestVersion: $rss")
        val request = StringRequest(
            Request.Method.GET, rss,
            { response ->
                try {
                    val parsed = RepoHelpers.parseRssValues(response)
                    metaData.latestVersion.value = parsed.latestVersion
                    metaData.latestVersionDate.value = parsed.latestVersionDate
                    metaData.latestVersionUrl.value = parsed.latestVersionUrl
                    if (RepoHelpers.isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value = MetaDataState.Loaded
                } catch (e: Exception) {
                    metaData.errors.add(e.localizedMessage ?: e.message ?: "Exception thrown while parsing response from $rss")
                    metaData.state.value = MetaDataState.Errored
                }
            },
            { error ->
                println(error)
                metaData.errors.add(error.localizedMessage ?: error.message ?: "Error occurred fetching $rss")
                metaData.state.value = MetaDataState.Errored
            })
        requestQueue.add(request)
    }
}
