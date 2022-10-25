package com.jroddev.android_oss_release_tracker.repo

import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object RepoHelpers {

    fun isLoaded(metaData: RepoMetaData): Boolean {
        return metaData.packageName.value != null && metaData.latestVersion.value != null
    }

    fun parsePackageNameFromBuildGradle(buildGradleContent: String): String? =  buildGradleContent
        .lines()
        .find { it.contains("applicationId") }
        ?.replace("applicationId", "")
        ?.replace("\"", "")
        ?.trim()


    // \x2E = literal . (dot)
    // https://f-droid.org/<anything>/(letters).(letters).(letters)
    const val README_REGEX =  "https://f-droid.org/.*/([a-z]+[\\x2E][a-z]+[\\x2E][a-z]+)"
    fun parsePackageNameFromREADME(readme: String): String? = README_REGEX
        .toRegex()
        .find(readme)
        ?.groups
        ?.get(1)
        ?.value


    // 0.24.0
    // drop the 'v' prefix if exists
    // from https://github.com/TeamNewPipe/NewPipe/releases.atom
    // https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0
    // derived from RSS
    // entry.title and entry.link.href
    data class RssValues(
        val latestVersion: String,
        val latestVersionDate: String,
        val latestVersionUrl: String
    )
    fun parseRssValues(rssXML: String): RssValues {
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val inputSource = InputSource(StringReader(rssXML))
        val doc = docBuilder.parse(inputSource)
        val feed = doc.getElementsByTagName("feed").item(0) as Element
        val entry = feed.getElementsByTagName("entry").item(0) as Element
        val title = entry.getElementsByTagName("title").item(0).textContent
        val updated = entry.getElementsByTagName("updated").item(0).textContent
        val updateLink = entry.getElementsByTagName("link")
            .item(0).attributes.getNamedItem("href").textContent

        val latestVersion = if (title.startsWith('v'))
            title.substring(1)
        else
            title
        return RssValues(
            latestVersion,
            latestVersionDate = updated,
            latestVersionUrl = updateLink
        )
    }


}