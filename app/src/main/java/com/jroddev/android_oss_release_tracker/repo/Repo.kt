package com.jroddev.android_oss_release_tracker.repo

interface Repo {
    suspend fun getPackageName(): String
    suspend fun getApplicationFriendlyName(): String
    suspend fun getLatestVersion(): String
    suspend fun getLinkToLatestVersion(): String
}