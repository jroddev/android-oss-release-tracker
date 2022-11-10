package com.jroddev.android_oss_release_tracker.repo

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class GitLab : CommonRepo() {

    override fun getUrlOfRawFile(org: String, app: String, branch: String, filepath: String): String {
        return "https://gitlab.com/$org/$app/-/raw/$branch/$filepath"
    }

    override fun getFileMetaDataUrl(
        org: String,
        app: String,
        branch: String,
        file: String
    ): String {
        // https://gitlab.com/api/v4/projects/AuroraOSS%2FAuroraStore/repository/files/app%2Fbuild.gradle?ref=master
        val repoEncoded = URLEncoder.encode("$org/$app", "utf-8")
        val fileEncoded = URLEncoder.encode(file, "utf-8")

        return "https://gitlab.com/api/v4/projects/$repoEncoded/repository/files/$fileEncoded?ref=$branch"
    }

    override fun getRepoMetaDataUrl(org: String, app: String): String {
        return "https://gitlab.com/api/v4/projects/$org%2F$app"
    }

    override fun getReadmeUrl(org: String, app: String): String {
        return "https://raw.githubusercontent.com/$org/$app/-/raw/master/README.md"
    }

    override fun getReleasesUrl(org: String, app: String): String {
        return "https://gitlab.com/api/v4/projects/$org%2F$app/releases"
    }

    override fun getRssFeedUrl(org: String, app: String): String {
        return "https://gitlab.com/$org/$app/-/tags?format=atom"
    }

    override fun parseReleasesJson(data: JSONArray): LatestVersionData {
        val firstEntry = data.get(0) as JSONObject

        // strip leading v from versions like v1.0.0
        val rawVersionString = firstEntry.getString("name")
        val latestVersion = if (rawVersionString.startsWith('v'))
            rawVersionString.substring(1)
        else
            rawVersionString

        return LatestVersionData(
            version = latestVersion,
            url = firstEntry.getJSONObject("_links").getString("self"),
            date = firstEntry.getString("released_at")
        )
    }

    override fun getIconUrl(repoUrl: String, branch: String, androidRoot: String): String {
        return "https://gitlab.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/-/raw/$branch/$androidRoot/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}