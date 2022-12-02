package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.android.volley.RequestQueue
import arrow.core.Either
import kotlinx.coroutines.*
import org.json.JSONArray
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

    val repo: Repo? = Repo.Helper.new(repoUrl)
    var orgName: String
    var appName: String
    val state = mutableStateOf(MetaDataState.Unsupported)
    var defaultBranch: String? = null
    var androidRoot: String? = "app"
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
                defaultBranch =
                    when (val result = repo.fetchBranchName(orgName, appName, requestQueue)) {
                        is Either.Left -> result.value
                        is Either.Right -> {
                            errors.add(result.value.message ?: "failed to retrieve defaultBranch")
                            state.value = MetaDataState.Errored
                            "master"
                        }
                    }
                println("defaultBranch $defaultBranch")

                androidRoot = repo.tryDetermineAndroidRoot(
                    orgName, appName,
                    defaultBranch!!, requestQueue
                )

                iconUrl.value = repo.getIconUrl(repoUrl, defaultBranch!!, androidRoot!!)
                println("iconUrl: ${iconUrl.value}")

                packageName.value = PackageNameResolver.tryResolve(this@RepoMetaData, requestQueue)
                if (packageName.value == null) {
                    state.value = MetaDataState.Errored
                    packageName.value = "<not found>"
                } else {
                    if (state.value != MetaDataState.Errored) {
                        state.value = MetaDataState.Loaded
                    }
                }
                println("package Name: ${packageName.value}")

                when (val result = repo.fetchLatestVersion(orgName, appName, requestQueue)) {
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
    fun getIconUrl(repoUrl: String, branch: String, androidRoot: String): String
    fun getUrlOfRawFile(org: String, app: String, branch: String, filepath: String): String
    suspend fun fetchBranchName(
        org: String,
        app: String,
        requestQueue: RequestQueue
    ): Either<String, Error>

    suspend fun fetchLatestVersion(
        org: String,
        app: String,
        requestQueue: RequestQueue
    ): Either<LatestVersionData, Error>

    suspend fun tryDetermineAndroidRoot(
        org: String,
        app: String,
        branch: String,
        requestQueue: RequestQueue
    ): String


    object Helper {
        fun new(repoUrl: String): Repo =
            if (repoUrl.contains("github")) {
                GitHub()
            } else {
                // If not GitHub, then we make an assumption that this is GitLab
                // Supports gitlab.com and self-hosted variants
                GitLab()
            }
    }
}


abstract class CommonRepo : Repo {

    abstract fun getFileMetaDataUrl(org: String, app: String, branch: String, file: String): String
    abstract fun getRepoMetaDataUrl(org: String, app: String): String
    abstract fun getReadmeUrl(org: String, app: String): String
    abstract fun getReleasesUrl(org: String, app: String): String
    abstract fun getRssFeedUrl(org: String, app: String): String
    abstract fun parseReleasesJson(data: JSONArray): LatestVersionData

    // Strip everything before the first number. Then any non-whitespace character is allowed
    val VERSION_NAME_REGEX = "([0-9]+\\S*)".toRegex()
    fun cleanVersionName(input: String): String =
        VERSION_NAME_REGEX.find(input)?.groups?.get(0)?.value ?: "unknown"

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

    override suspend fun fetchBranchName(
        org: String,
        app: String,
        requestQueue: RequestQueue
    ): Either<String, Error> {
        val url = getRepoMetaDataUrl(org, app)
        return when (val response = ApiUtils.getJsonObject(url, requestQueue)) {
            is Either.Left -> {
                try {
                    Either.Left(response.value.getString("default_branch"))
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

    override suspend fun fetchLatestVersion(
        org: String,
        app: String,
        requestQueue: RequestQueue
    ): Either<LatestVersionData, Error> {
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

    override suspend fun tryDetermineAndroidRoot(
        org: String,
        app: String,
        branch: String,
        requestQueue: RequestQueue
    ): String {
        val candidates = listOf("app", "android/app")
        candidates.forEach { candidate ->
            if (ApiUtils.get(
                    getFileMetaDataUrl(org, app, branch, "$candidate/build.gradle"),
                    requestQueue
                ).isLeft()
            ) {
                println("Android Root for $org/$app detected as $candidate")
                return candidate
            }
        }
        println("Android Root for $org/$app not found")
        return ""
    }
}
