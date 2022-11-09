package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.android.volley.RequestQueue
import arrow.core.Either
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

enum class MetaDataState {
    Unsupported,
    Loading,
    Errored,
    Loaded
}

data class LatestVersionData(
    val version: String,
    val url: String,
    val date: String
)


data class RepoMetaData(
    val repoUrl: String,
    val requestQueue: RequestQueue,
    ) {

    private val repo: Repo? = Repo.Helper.new(repoUrl)
    var orgName: String
    var appName: String
    val state = mutableStateOf(MetaDataState.Unsupported)
    var iconUrl = mutableStateOf<String?>(null)
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
        } else {
            state.value = MetaDataState.Loading
            orgName = repo.getOrgName(repoUrl)
            appName = repo.getApplicationName(repoUrl)

            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                println("Starting API calls for $repoUrl")
                val defaultBranch = when (val result = repo.fetchBranchName(orgName, appName, requestQueue)) {
                    is Either.Left -> result.value
                    is Either.Right -> {
                        errors.add(result.value.message ?: "failed to retrieve defaultBranch")
                        state.value = MetaDataState.Errored
                        "master"
                    }
                }
                println("defaultBranch $defaultBranch")

                iconUrl.value = repo.getIconUrl(repoUrl, defaultBranch)
                println("iconUrl: ${iconUrl.value}")

                packageName.value = when(val result = repo.fetchPackageName(orgName, appName, defaultBranch, requestQueue)) {
                    is Either.Left -> {
                        if (state.value != MetaDataState.Errored) {
                            state.value = MetaDataState.Loaded
                        }
                        result.value
                    }
                    is Either.Right -> {
                        errors.add(result.value.message ?: "failed to retrieve packageName")
                        state.value = MetaDataState.Errored
                        "<not found>"
                    }
                }
                println("package Name: ${packageName.value}")

                when(val result = repo.fetchLatestVersion(orgName, appName, requestQueue)) {
                    is Either.Left -> {
                        println("latestVersion: ${result.value}")
                        latestVersion.value = result.value.version
                        latestVersionDate.value = result.value.date
                        latestVersionUrl.value = result.value.url
                    }
                    is Either.Right -> {
                        errors.add(result.value.message ?: "failed to retrieve latestVersion")
                        state.value = MetaDataState.Errored
                    }
                }
            }

        }
    }
}


interface Repo {
    fun getOrgName(repoUrl: String): String
    fun getApplicationName(repoUrl: String): String
    fun getIconUrl(repoUrl: String, branch: String): String
    suspend fun fetchBranchName(org: String, app: String, requestQueue: RequestQueue): Either<String, Error>
    suspend fun fetchPackageName(org: String, app: String, branch: String, requestQueue: RequestQueue): Either<String, Error>
    suspend fun fetchLatestVersion(org: String, app: String, requestQueue: RequestQueue): Either<LatestVersionData, Error>

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
    abstract fun getBranchesUrl(org: String, app: String): String
    abstract fun getBuildGradleUrl(org: String, app: String, branch: String): String
    abstract fun getReadmeUrl(org: String, app: String): String
    abstract fun getReleasesUrl(org: String, app: String): String
    abstract fun getRssFeedUrl(org: String, app: String): String
    abstract fun parseReleasesJson(data: JSONArray): LatestVersionData

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

    override suspend fun fetchBranchName(org: String, app: String, requestQueue: RequestQueue): Either<String, Error> {
        val url = getBranchesUrl(org, app)
        return when (val response = ApiUtils.getJsonArray(url, requestQueue)) {
            is Either.Left -> {
                try {
                    Either.Left((response.value.get(0) as JSONObject).getString("name"))
                } catch (e: Exception) {
                    println(e)
                    Either.Right(Error("Could not parse result of fetchBranchName api call"))
                }
            }
            is Either.Right -> {
                println(response.value)
                Either.Right(Error(response.value))
            }
        }
    }

    override suspend fun fetchPackageName(org: String, app: String, branch: String, requestQueue: RequestQueue): Either<String, Error> {
        return try {
            val firstUrl = getBuildGradleUrl(org, app, branch)
            val firstAttempt = when (val response = ApiUtils.get(firstUrl, requestQueue)) {
                is Either.Left -> {

                        val appId = RepoHelpers.parsePackageNameFromBuildGradle(response.value)
                        if (appId.isNullOrEmpty()) {
                            throw Error("Error occurred parsing $firstUrl")
                        } else {
                            Either.Left(appId)
                        }
                }
                is Either.Right -> {
                    println(response.value)
                    Either.Right(Error(response.value))
                }
            }

            when(firstAttempt) {
                is Either.Left -> return firstAttempt
                is Either.Right -> {
                    // Try fallback method
                    // Some READMEs in these repositories have links to F-droid and those urls contain the package name
                    val fallbackUrl = getReadmeUrl(org, app,)
                    when (val response = ApiUtils.get(fallbackUrl, requestQueue)) {
                        is Either.Left -> {
                            val appId = RepoHelpers.parsePackageNameFromREADME(response.value)
                            if (appId.isNullOrEmpty()) {
                                throw Error("Error occurred parsing $firstUrl")
                            } else {
                                Either.Left(appId)
                            }
                        }
                        is Either.Right -> {
                            println(response.value)
                            Either.Right(Error(response.value))
                        }
                    }
                }
            }
        }  catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Error occurred fetching PackageName"
            println(msg)
            Either.Right(Error(msg))
        }
    }

    override suspend fun fetchLatestVersion(org: String, app: String, requestQueue: RequestQueue): Either<LatestVersionData, Error> {
        val url = getReleasesUrl(org, app)
        return when (val response = ApiUtils.getJsonArray(url, requestQueue)) {
            is Either.Left -> {
                try {
                    val parsed = parseReleasesJson(response.value)
                    Either.Left(parsed)
                } catch (e: Exception) {
                    println(e)
                    Either.Right(Error("Could not parse result of fetchLatestVersion api call"))
                }
            }
            is Either.Right -> {
                println(response.value)
                Either.Right(Error(response.value))
            }
        }
    }
}
