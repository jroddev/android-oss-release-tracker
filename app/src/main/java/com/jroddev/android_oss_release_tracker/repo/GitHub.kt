package com.jroddev.android_oss_release_tracker.repo

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

// https://github.com/TeamNewPipe/NewPipe
class GitHub : Repo {

    override fun getOrgName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[1]
    }

    // NewPipe
    // from repo URL https://github.com/TeamNewPipe/NewPipe
    override fun getApplicationName(repoUrl: String): String {
        val url = URL(repoUrl)
        return url.path.split("/")[2]
    }

    // e.g. https://github.com/bitfireAT/davx5-ose/raw/dev-ose/app/src/main/res/mipmap-mdpi/ic_launcher.png
    // e.g. https://github.com/TeamNewPipe/NewPipe/raw/dev/app/src/main/res/mipmap-mdpi/ic_launcher.png
    override fun fetchIconUrl(repoUrl: String): String {
        val dev = if (repoUrl.lowercase().contains("-ose")) "dev-ose" else "dev"
        return "https://github.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/raw/$dev/app/src/main/res/mipmap-mdpi/ic_launcher.png"
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
                    val appId = response
                        .lines()
                        .find { it.contains("applicationId") }
                        ?.replace("applicationId", "")
                        ?.replace("\"", "")
                        ?.trim()
                    println("APP ID: $appId")
                    metaData.packageName.value = appId
                    if (isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value = MetaDataState.Loaded
                }  catch (e: Exception) {
                    println(e)
                    metaData.errors.add(e.localizedMessage ?: e.message ?: "Error occurred parsing $url")
                    metaData.state.value = MetaDataState.Errored
                }
            },
            { error ->
                println(error)
                metaData.errors.add(error.localizedMessage ?: error.message ?: "Error occurred fetching $url")
//                metaData.state.value = MetaDataState.Errored
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
                    // \x2E = literal . (dot)
                    // https://f-droid.org/<anything>/(letters).(letters).(letters)
                    val appId = "https://f-droid.org/.*/([a-z]+[\\x2E][a-z]+[\\x2E][a-z]+)"
                        .toRegex()
                        .find(response)
                        ?.groups
                        ?.get(1)
                        ?.value
                    println("APP D: $appId")

                    metaData.packageName.value = appId
                    if (isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value =
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


    // 0.24.0
    // drop the 'v' prefix if exists
    // from https://github.com/TeamNewPipe/NewPipe/releases.atom
    // https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0
    // derived from RSS
    // entry.title and entry.link.href
    override fun fetchLatestVersion(metaData: RepoMetaData, requestQueue: RequestQueue) {
        val rss = "https://github.com/${getOrgName(metaData.repoUrl)}/${getApplicationName(metaData.repoUrl)}/releases.atom"
        println("getLatestVersion: $rss")
        val request = StringRequest(Request.Method.GET, rss,
            { response ->
                try {
                    val builderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder = builderFactory.newDocumentBuilder()
                    val inputSource = InputSource(StringReader(response))
                    val doc = docBuilder.parse(inputSource)
                    val feed = doc.getElementsByTagName("feed").item(0) as Element
                    val entry = feed.getElementsByTagName("entry").item(0) as Element
                    val title = entry.getElementsByTagName("title").item(0).textContent
                    val updated = entry.getElementsByTagName("updated").item(0).textContent
                    val updateLink = entry.getElementsByTagName("link")
                        .item(0).attributes.getNamedItem("href").textContent

                    metaData.latestVersion.value = if (title.startsWith('v'))
                        title.substring(1)
                    else
                        title
                    metaData.latestVersionDate.value = updated
                    metaData.latestVersionUrl.value = updateLink

                    if (isLoaded(metaData) && metaData.state.value != MetaDataState.Errored) metaData.state.value = MetaDataState.Loaded
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

    private fun isLoaded(metaData: RepoMetaData): Boolean {
        return metaData.packageName.value != null && metaData.latestVersion.value != null
    }
}