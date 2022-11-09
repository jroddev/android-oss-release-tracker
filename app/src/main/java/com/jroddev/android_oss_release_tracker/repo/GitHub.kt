package com.jroddev.android_oss_release_tracker.repo

import org.json.JSONArray
import org.json.JSONObject

class GitHub : CommonRepo() {
    override fun getBranchesUrl(org: String, app: String): String {
        return "https://api.github.com/repos/$org/$app/branches"
    }

    override fun getBuildGradleUrl(org: String, app: String, branch: String): String {
        return "https://raw.githubusercontent.com/$org/$app/$branch/app/build.gradle"
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
            version = firstEntry.getString("name"),
            url = firstEntry.getString("html_url"),
            date = firstEntry.getString("published_at")
        )
    }

    override fun getIconUrl(repoUrl: String, branch: String): String {
        return "https://github.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/raw/$branch/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}