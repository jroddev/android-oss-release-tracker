package com.jroddev.android_oss_release_tracker.repo

interface Repo {
    suspend fun getPackageName(): String
    suspend fun getApplicationName(): String
    suspend fun getLatestVersion(): String
    suspend fun getLinkToLatestVersion(): String
    suspend fun getIconUrl(): String
}