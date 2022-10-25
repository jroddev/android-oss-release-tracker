package com.jroddev.android_oss_release_tracker.repo

class GitLab : CommonRepo() {
    override fun getBuildGradleUrl(org: String, app: String): String {
        return  "https://gitlab.com/$org/$app/-/raw/master/app/build.gradle"
    }

    override fun getReadmeUrl(org: String, app: String): String {
        return "https://raw.githubusercontent.com/$org/$app/-/raw/master/README.md"
    }

    override fun getRssFeedUrl(org: String, app: String): String {
        return "https://gitlab.com/$org/$app/-/tags?format=atom"
    }

    override fun getIconUrl(repoUrl: String): String {
        return "https://gitlab.com/${getOrgName(repoUrl)}/${getApplicationName(repoUrl)}/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png"
    }
}