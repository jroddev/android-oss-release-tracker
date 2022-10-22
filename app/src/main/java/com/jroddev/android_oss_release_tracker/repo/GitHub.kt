package com.jroddev.android_oss_release_tracker.repo

// https://github.com/TeamNewPipe/NewPipe
class GitHub(repoUrl: String) : Repo {

    // org.schabi.newpipe
    // from https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle
    // applicationId "org.schabi.newpipe"
    override suspend fun getPackageName(): String {
        TODO("Not yet implemented")
    }

    // NewPipe
    // from repo URL https://github.com/TeamNewPipe/NewPipe
    override suspend fun getApplicationFriendlyName(): String {
        TODO("Not yet implemented")
    }

    // 0.24.0
    // drop the 'v' prefix if exists
    // from https://github.com/TeamNewPipe/NewPipe/releases.atom
    override suspend fun getLatestVersion(): String {
        TODO("Not yet implemented")
    }

    // https://github.com/TeamNewPipe/NewPipe/releases/tag/v0.24.0
    // derived from RSS
    override suspend fun getLinkToLatestVersion(): String {
        TODO("Not yet implemented")
    }
}