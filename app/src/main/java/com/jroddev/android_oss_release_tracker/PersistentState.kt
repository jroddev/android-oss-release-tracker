package com.jroddev.android_oss_release_tracker

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

object PersistentState {

    const val STATE_FILENAME = "PersistedState"
    private const val APP_TRACKERS = "app_trackers"
    private val defaultTrackers = setOf("https://github.com/jroddev/android-oss-release-tracker")

    fun getSavedTrackers(sharedPreferences: SharedPreferences): Set<String> {
        return sharedPreferences.getStringSet("app_trackers", defaultTrackers)!!
    }

    fun addTracker(ctx: Context, sharedPreferences: SharedPreferences, appName: String, repo: String) {
        println("Add $repo")
        val existing = sharedPreferences.getStringSet(APP_TRACKERS, defaultTrackers)!!
        val newList = mutableSetOf<String>()
        newList.addAll(existing)
        newList.add(repo)
        newList.remove("")
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
        Toast.makeText(ctx, "Added $appName to your trackers", Toast.LENGTH_LONG).show()
    }

    fun addTrackers(ctx: Context, sharedPreferences: SharedPreferences, repos: List<String>) {
        println("Adding ${repos.size} trackers")
        val existing = sharedPreferences.getStringSet(APP_TRACKERS, defaultTrackers)!!
        val newList = mutableSetOf<String>()
        newList.addAll(existing)
        newList.addAll(repos)
        println("New set: $newList")
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
        Toast.makeText(ctx, "Added ${repos.size} trackers", Toast.LENGTH_LONG).show()
    }

    fun removeTracker(ctx: Context, sharedPreferences: SharedPreferences, appName: String, repo: String) {
        println("Remove $repo")
        val existing = sharedPreferences.getStringSet(APP_TRACKERS, defaultTrackers)!!
        val newList = mutableSetOf<String>()
        newList.addAll(existing)
        newList.remove(repo)
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
        Toast.makeText(ctx, "Deleted $appName from your trackers", Toast.LENGTH_LONG).show()
    }

    fun removeAllTrackers(ctx: Context, sharedPreferences: SharedPreferences) {
        println("Removing all Trackers")
        val newList = mutableSetOf<String>()
        val editor = sharedPreferences.edit()
        editor.putStringSet("app_trackers", newList)
        editor.apply()
        Toast.makeText(ctx, "Deleted all trackers", Toast.LENGTH_LONG).show()
    }

}