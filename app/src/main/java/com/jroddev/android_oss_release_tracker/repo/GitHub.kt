package com.jroddev.android_oss_release_tracker.repo

import org.json.JSONArray
import org.json.JSONObject

class GitHub : CommonRepo() {

    override fun getUrlOfRawFile(org: String, app: String, branch: String, filepath: String): String {
        return "https://raw.githubusercontent.com/$org/$app/$branch/$filepath"
    }

    override fun getFileMetaDataUrl(
        org: String,
        app: String,
        branch: String,
        file: String
    ): String {
        // https://api.github.com/repos/ImranR98/Obtainium/contents/android/app
        return "https://api.github.com/repos/$org/$app/contents/$file"
    }

    override fun getRepoMetaDataUrl(org: String, app: String): String {
        return "https://api.github.com/repos/$org/$app"
    }

    override fun getReadmeUrl(org: String, app: String): String {
        return "https://raw.githubusercontent.com/$org/$app/master/README.md"
    }

    override fun getReleasesUrl(org: String, app: String): String {
        return "https://api.github.com/repos/$org/$app/releases"
    }

    override fun getRssFeedUrl(org: String, app: String): String {
        return "https://github.com/$org/$app/releases.atom"
    }

    override fun parseReleasesJson(data: JSONArray): LatestVersionData {
        val firstEntry = data.get(0) as JSONObject

        return LatestVersionData(
            version = cleanVersionName(firstEntry.getString("name")) ?: cleanVersionName(firstEntry.getString("tag_name"))  ?: "unknown",
            url = firstEntry.getString("html_url"),
            date = firstEntry.getString("published_at")
        )
    }

    override fun getIconUrl(repoUrl: String, branch: String, androidRoot: String): String {
        return "https://github.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/raw/$branch/$androidRoot/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}
