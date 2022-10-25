package com.jroddev.android_oss_release_tracker.repo

import androidx.compose.runtime.MutableState
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import java.net.URL

// example: https://gitlab.com/AuroraOSS/AuroraStore
class GitLab : Repo {

    override fun getOrgName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[1]
    }

    // NewPipe
    // from repo URL https://gitlab.com/AuroraOSS/AuroraStore
    override fun getApplicationName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[2]
    }

    // e.g. https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png
    override fun fetchIconUrl(repoUrl: String): String {
        return "https://gitlab.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }

    // com.aurora.store
    // from https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/build.gradle
    // applicationId "com.aurora.store"
    override fun fetchPackageName(metaData: RepoMetaData, requestQueue: RequestQueue) {
        val url = "https://gitlab.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/-/raw/master/app/build.gradle"
        val request = StringRequest(Request.Method.GET, url,
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
        val url = "https://raw.githubusercontent.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/-/raw/master/README.md"
        println("running fallbackPackageName. url: $url")
        val request = StringRequest(Request.Method.GET, url,
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
        val rss = "https://gitlab.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/-/tags?format=atom"
        println("getLatestVersion: $rss")
        val request = StringRequest(Request.Method.GET, rss,
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


//    override fun getOrgName(): String {
//        val url = URL(repoUrl)
//        return url.path.split("/")[1]
//    }
//
//
//    // Aurora Store
//    // from repo URL https://github.com/AuroraOSS/AuroraStore
//    override fun getApplicationName(): String {
//        val url = URL(repoUrl)
//        return url.path.split("/")[2]
//    }
//
//    // 4.1.1
//    // from https://gitlab.com/AuroraOSS/AuroraStore/-/tags?format=atom
//    override fun getLatestVersion(output: MutableState<String?>) {
//        TODO("Not yet implemented")
//    }
//
//    // https://gitlab.com/AuroraOSS/AuroraStore/-/releases/4.1.1
//    // derived from RSS
//    override fun getLinkToLatestVersion(output: MutableState<String?>) {
//        TODO("Not yet implemented")
//    }

}