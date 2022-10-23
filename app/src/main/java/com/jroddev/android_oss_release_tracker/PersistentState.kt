package com.jroddev.android_oss_release_tracker

import android.content.SharedPreferences

object PersistentState {

    const val STATE_FILENAME = "PersistedState"
    private const val APP_TRACKERS = "app_trackers"
    private val defaultTrackers = setOf("https://github.com/jroddev/android-oss-release-tracker")

    fun getSavedTrackers(sharedPreferences: SharedPreferences): Set<String> {
        return sharedPreferences.getStringSet("app_trackers", defaultTrackers)!!
    }

    fun addTracker(sharedPreferences: SharedPreferences, repo: String) {
        println("Add $repo")
        val existing = sharedPreferences.getStringSet(APP_TRACKERS, defaultTrackers)!!
        val newList = mutableSetOf<String>()
        newList.addAll(existing)
        newList.add(repo)
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
    }

    fun removeTracker(sharedPreferences: SharedPreferences, repo: String) {
        println("Remove $repo")
        val existing = sharedPreferences.getStringSet(APP_TRACKERS, defaultTrackers)!!
        val newList = mutableSetOf<String>()
        newList.addAll(existing)
        newList.remove(repo)
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
    }

}