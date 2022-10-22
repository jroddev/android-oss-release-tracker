package com.jroddev.android_oss_release_tracker.repo

import java.util.Optional

// https://github.com/TeamNewPipe/NewPipe
class GitHub(repoUrl: String) : Repo {

    init {
        // fetch all the things?
    }

    // org.schabi.newpipe
    // from https://raw.githubusercontent.com/TeamNewPipe/NewPipe/dev/app/build.gradle
    // applicationId "org.schabi.newpipe"
    override suspend fun getPackageName(): String {
        TODO("Not yet implemented")
    }

    // NewPipe
    // from repo URL https://github.com/TeamNewPipe/NewPipe
    override suspend fun getApplicationName(): String {
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

    // e.g. https://github.com/bitfireAT/davx5-ose/raw/dev-ose/app/src/main/res/mipmap-mdpi/ic_launcher.png
    // e.g. https://github.com/TeamNewPipe/NewPipe/raw/dev/app/src/main/res/mipmap-mdpi/ic_launcher.png
    override suspend fun getIconUrl(): String {
        TODO("Not yet implemented")
    }
}