package com.jroddev.android_oss_release_tracker.repo

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import java.net.URL

class GitHub : CommonRepo() {

    override fun getBuildGradleUrl(org: String, app: String): String {
        val dev = if (app.lowercase().contains("-ose")) "dev-ose" else "dev"
        return "https://raw.githubusercontent.com/$org/$app/${dev}/app/build.gradle"
    }

    override fun getReadmeUrl(org: String, app: String): String {
        return "https://raw.githubusercontent.com/$org/$app/master/README.md"
    }

    override fun getRssFeedUrl(org: String, app: String): String {
        return "https://github.com/$org/$app/releases.atom"
    }

    override fun getIconUrl(repoUrl: String): String {
        val dev = if (repoUrl.lowercase().contains("-ose")) "dev-ose" else "dev"
        return "https://github.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/raw/$dev/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }


    override fun getOrgName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[1]
    }

    // org.schabi.newpipe
    // from https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle
    // applicationId "org.schabi.newpipe"
    override fun fetchPackageName(metaData: RepoMetaData, requestQueue: RequestQueue) {
        val dev = if (metaData.repoUrl.lowercase().contains("-ose")) "dev-ose" else "dev"
        val url = "https://raw.githubusercontent.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/${dev}/app/build.gradle"
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
        val url = "https://raw.githubusercontent.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/master/README.md"
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
        val rss = "https://github.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/releases.atom"
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
}