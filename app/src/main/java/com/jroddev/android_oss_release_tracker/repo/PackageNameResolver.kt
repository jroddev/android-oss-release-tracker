package com.jroddev.android_oss_release_tracker.repo

import arrow.core.Either
import com.android.volley.RequestQueue

object PackageNameResolver {

    suspend fun tryResolve(data: RepoMetaData, requestQueue: RequestQueue): String? {
        val filesToCheck = listOf(
            "app/build.gradle",
            "android/app/build.gradle",
            "README.md"
        )

        filesToCheck.forEach { file ->
            try {
                val url = data.repo!!.getUrlOfRawFile(data.orgName, data.appName, data.defaultBranch!!, file)
                when (val content = ApiUtils.get(url, requestQueue)) {
                    is Either.Left -> {
                        val result = tryParseFile(content.value)
                        if (result == null) {
                            println("Exhausted parsers trying to determine packageName for $url")
                        }
                        return result
                    }
                    is Either.Right -> {
                        println("Exception retrieving $url")
                        data.errors.add(content.value.toString())

                    }
                }
            } catch (e: Exception) {
                println("Exception through trying to parse $file")
                data.errors.add(e.toString())
            }
        }

        println("Exhausted all attempts to try to determine packageName for ${data.orgName}/${data.appName}")
        return null
    }


    private fun tryParseFile(content: String): String? {
        val parsers = listOf(
            { input: String -> PackageNameParsers.tryFindApplicationId(input) },
            { input: String -> PackageNameParsers.tryFindReverseDomain(input) },
            { input: String -> PackageNameParsers.tryFindFdroidLink(input) },
        )
        parsers.forEach { parser ->
            val result = parser(content)
            if (result != null) {
                return result
            }
        }
        return null
    }

}

object PackageNameParsers {

    // \x2E = literal . (dot)
    // "(letters).(letters).(letters)" e.g. "com.jroddev.android-oss-release-tracker"
    val REVERSE_DOMAIN_STRING_REGEX = "\"([A-Za-z]+[\\x2E][A-Za-z]+[\\x2E][A-Za-z]+)\"".toRegex()

    // \x2E = literal . (dot)
    // https://f-droid.org/<anything>/(letters).(letters).(letters)
    val FDROID_REGEX =  "https://f-droid.org/.*/([a-z]+[\\x2E][a-z]+[\\x2E][a-z]+)".toRegex()

    fun tryFindApplicationId(content: String): String? = content
        .lines()
        .find { it.contains("applicationId") && REVERSE_DOMAIN_STRING_REGEX.matches(it) }
        ?.replace("applicationId", "")
        ?.replace("\"", "")
        ?.trim()

    fun tryFindReverseDomain(content: String): String? = REVERSE_DOMAIN_STRING_REGEX
        .find(content)
        ?.groups
        ?.get(1)
        ?.value

    fun tryFindFdroidLink(content: String): String? = FDROID_REGEX
        .find(content)
        ?.groups
        ?.get(1)
        ?.value

}