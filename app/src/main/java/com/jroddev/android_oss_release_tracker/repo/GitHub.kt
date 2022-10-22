package com.jroddev.android_oss_release_tracker.repo

import android.util.Xml
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xmlpull.v1.XmlPullParser
import java.io.Reader
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

// https://github.com/TeamNewPipe/NewPipe
class GitHub(val repoUrl: String, val requestQueue: RequestQueue) : Repo {


    // org.schabi.newpipe
    // from https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle
    // applicationId "org.schabi.newpipe"
    override fun getPackageName(output: MutableState<String?>, errors: SnapshotStateList<String>) {
        val dev = if (repoUrl.lowercase().contains("-ose")) "dev-ose" else "dev"
        val url = "https://raw.githubusercontent.com/${getOrgName()}/${getApplicationName()}/${dev}/app/build.gradle"
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                val appId = response
                    .lines()
                    .find { it.contains("applicationId") }
                    ?.replace("applicationId", "")
                    ?.replace("\"", "")
                    ?.trim()
                println("APP ID: $appId")
                output.value = appId
            },
            { error ->
                println(error)
                errors.add(error.localizedMessage!!)
            })
        requestQueue.add(request)
    }

    override fun getOrgName(): String {
        val url = URL(repoUrl)
        return url.path.split("/")[1]
    }

    // NewPipe
    // from repo URL https://github.com/TeamNewPipe/NewPipe
    override fun getApplicationName(): String {
        val url = URL(repoUrl)
        return url.path.split("/")[2]
   }

    // 0.24.0
    // drop the 'v' prefix if exists
    // from https://github.com/TeamNewPipe/NewPipe/releases.atom
    // https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0
    // derived from RSS
    // entry.title and entry.link.href
    override fun getLatestVersion(
        version: MutableState<String?>,
        lastUpdated: MutableState<String?>,
        link: MutableState<String?>, errors: SnapshotStateList<String>) {
        val rss = "https://github.com/${getOrgName()}/${getApplicationName()}/releases.atom"
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

                    if (title.startsWith('v')) {
                        version.value = title.substring(1)
                    } else {
                        version.value = title
                    }
                    lastUpdated.value = updated
                    link.value = updateLink
                } catch (e: Exception) {
                    errors.add(e.localizedMessage!!)
                }
            },
            { error ->
                println(error)
                errors.add(error.localizedMessage!!)
            })
        requestQueue.add(request)
    }


    // e.g. https://github.com/bitfireAT/davx5-ose/raw/dev-ose/app/src/main/res/mipmap-mdpi/ic_launcher.png
    // e.g. https://github.com/TeamNewPipe/NewPipe/raw/dev/app/src/main/res/mipmap-mdpi/ic_launcher.png
    override fun getIconUrl(): String {
        val dev = if (repoUrl.lowercase().contains("-ose")) "dev-ose" else "dev"
        return "https://github.com/${getOrgName()}/${getApplicationName()}/raw/$dev/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}