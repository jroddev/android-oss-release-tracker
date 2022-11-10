package com.jroddev.android_oss_release_tracker.repo

object RepoHelpers {

    const val REVERSE_DOMAIN_STRING_REGEX = "\"([A-Za-z]+[\\x2E][A-Za-z]+[\\x2E][A-Za-z]+)\""
    fun parsePackageNameFromBuildGradle(buildGradleContent: String): String? =  buildGradleContent
        .lines()
        .find { it.contains("applicationId") }
        ?.replace("applicationId", "")
        ?.replace("\"", "")
        ?.trim()


    // \x2E = literal . (dot)
    // https://f-droid.org/<anything>/(letters).(letters).(letters)
    const val README_REGEX =  "https://f-droid.org/.*/([a-z]+[\\x2E][a-z]+[\\x2E][a-z]+)"
    fun parsePackageNameFromREADME(readme: String): String? = README_REGEX
        .toRegex()
        .find(readme)
        ?.groups
        ?.get(1)
        ?.value

}