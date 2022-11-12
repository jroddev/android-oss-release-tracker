package com.jroddev.android_oss_release_tracker.repo

import arrow.core.Either
import com.android.volley.RequestQueue

object PackageNameResolver {

    suspend fun tryResolve(data: RepoMetaData, requestQueue: RequestQueue): String? {
        val filesToCheck = listOf(
            "${data.androidRoot}/build.gradle",
//            "README.md"
        )

        filesToCheck.forEach { file ->
            try {
                val url = data.repo!!.getUrlOfRawFile(data.orgName, data.appName, data.defaultBranch!!, file)
                when (val content = ApiUtils.get(url, requestQueue)) {
                    is Either.Left -> {
                        println("success retrieving url: ${content.value}")
                        val result = tryParseFile(content.value)
                        if (result == null) {
                            println("Exhausted parsers trying to determine packageName for $url")
                        } else {
                            return result
                        }
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

    // Filter out some common lines that can confuse the parser
    private fun cleanFile(input: String): String =
        input.lines().filter {
            !it.contains("apply ") &&
            !it.contains("mplementation ") &&
            !it.contains("group: ") &&
            !"[ \\t]*id .*".toRegex().matches(it)
        }.joinToString("\n")


    private fun tryParseFile(content: String): String? {
        val cleanedContent = cleanFile(content)
        val parsers = listOf(
            { input: String -> PackageNameParsers.tryFindApplicationId(input) },
            { input: String -> PackageNameParsers.tryFindReverseDomain(input) },
            { input: String -> PackageNameParsers.tryFindFdroidLink(input) },
        )
        parsers.forEach { parser ->
            val result = parser(cleanedContent)
            println("cleanedContent: $cleanedContent")
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
    // letters also includes - and _ in this case
//    val REVERSE_DOMAIN_STRING_REGEX = ".*[\"']([A-Za-z-_]+[\\x2E][A-Za-z-_]+[\\x2E][A-Za-z-_]+)[\"'].*".toRegex()

    // experiment for packages with more or less componenets e.g. io.github.muntashirakon.AppManager or com.gh4a
    // <anything><" or '>(<two or more letters>.<<two or more letters, numbers, - or _> one or more times>
    val REVERSE_DOMAIN_STRING_REGEX = ".*[\"']([A-Za-z]{2,}(?:\\x2E[A-Za-z0-9-_]{2,})+)[\"']\\s*".toRegex()


    // \x2E = literal . (dot)
    // https://f-droid.org/<anything>/(letters, numbers).(letters, numbers, dot)
    val FDROID_REGEX =  "https://f-droid.org/.*/([A-Za-z0-9]+\\x2E[A-Za-z0-9\\x2E]+)".toRegex()

    fun tryFindApplicationId(content: String): String? = content
        .lines()
        .find {
            it.contains("applicationId ") &&
            REVERSE_DOMAIN_STRING_REGEX.matches(it)
        }
        ?.replace("applicationId", "")
        ?.replace("\"", "")
        ?.replace("\'", "")
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