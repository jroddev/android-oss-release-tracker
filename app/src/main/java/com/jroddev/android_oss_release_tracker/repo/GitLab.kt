//package com.jroddev.android_oss_release_tracker.repo
//
//import androidx.compose.runtime.MutableState
//import com.android.volley.Request
//import com.android.volley.RequestQueue
//import com.android.volley.toolbox.StringRequest
//import java.net.URL
//
//// https://gitlab.com/AuroraOSS/AuroraStore
//class GitLab(val repoUrl: String, val requestQueue: RequestQueue) : Repo {
//
//    // com.aurora.store
//    // from https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/build.gradle
//    // applicationId "com.aurora.store"
//    override fun getPackageName(output: MutableState<String?>) {
//        val url = "https://gitlab.com/${getOrgName()}/${getApplicationName()}/-/raw/master/app/build.gradle"
//        val request = StringRequest(Request.Method.GET, url,
//            { response ->
//                val appId = response
//                    .lines()
//                    .find { it.contains("applicationId") }
//                    ?.replace("applicationId", "")
//                    ?.replace("\"", "")
//                    ?.trim()
//                println("APP ID: $appId")
//                output.value = appId
//            },
//            { error ->
//                println(error)
//            })
//        requestQueue.add(request)
//    }
//
//    override fun getOrgName(): String {
//        val url = URL(repoUrl)
//        return url.path.split("/")[1]
//    }
//
//
//    // Aurora Store
//    // from repo URL https://github.com/AuroraOSS/AuroraStore
//    override fun getApplicationName(): String {
//        val url = URL(repoUrl)
//        return url.path.split("/")[2]
//    }
//
//    // 4.1.1
//    // from https://gitlab.com/AuroraOSS/AuroraStore/-/tags?format=atom
//    override fun getLatestVersion(output: MutableState<String?>) {
//        TODO("Not yet implemented")
//    }
//
//    // https://gitlab.com/AuroraOSS/AuroraStore/-/releases/4.1.1
//    // derived from RSS
//    override fun getLinkToLatestVersion(output: MutableState<String?>) {
//        TODO("Not yet implemented")
//    }
//
//    // e.g. https://gitlab.com/AuroraOSS/AuroraStore/-/raw/master/app/src/main/res/mipmap-mdpi/ic_launcher.png
//    override fun getIconUrl(): String {
//        TODO("Not yet implemented")
//    }
//}