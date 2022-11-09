package com.jroddev.android_oss_release_tracker.repo

import org.json.JSONArray
import org.json.JSONObject

class GitLab : CommonRepo() {
    override fun getBranchesUrl(org: String, app: String): String {
        return "https://gitlab.com/api/v4/projects/$org%2F$app/repository/branches"
    }

    override fun getBuildGradleUrl(org: String, app: String, branch: String): String {
        return  "https://gitlab.com/$org/$app/-/raw/$branch/app/build.gradle"
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
        return LatestVersionData(
            version = firstEntry.getString("name"),
            url = firstEntry.getJSONObject("_links").getString("self"),
            date = firstEntry.getString("released_at")
        )
    }

    override fun getIconUrl(repoUrl: String, branch: String): String {
        return "https://gitlab.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/-/raw/$branch/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}