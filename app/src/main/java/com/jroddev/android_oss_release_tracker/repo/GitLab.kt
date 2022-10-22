package com.jroddev.android_oss_release_tracker.repo

// https://gitlab.com/AuroraOSS/AuroraStore
class GitLab(repoUrl: String) : Repo {

    // com.aurora.store
    // from https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/build.gradle
    // applicationId "com.aurora.store"
    override suspend fun getPackageName(): String {
        TODO("Not yet implemented")
    }

    // Aurora Store
    // from repo URL https://github.com/AuroraOSS/AuroraStore
    override suspend fun getApplicationName(): String {
        TODO("Not yet implemented")
    }

    // 4.1.1
    // from https://gitlab.com/AuroraOSS/AuroraStore/-/tags?format=atom
    override suspend fun getLatestVersion(): String {
        TODO("Not yet implemented")
    }

    // https://gitlab.com/AuroraOSS/AuroraStore/-/releases/4.1.1
    // derived from RSS
    override suspend fun getLinkToLatestVersion(): String {
        TODO("Not yet implemented")
    }

    // e.g. https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png
    override suspend fun getIconUrl(): String {
        TODO("Not yet implemented")
    }
}