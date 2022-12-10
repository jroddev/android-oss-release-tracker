# Android OSS Release Tracker
<img src="https://github.com/jroddev/android-oss-release-tracker/blob/master/app/src/main/ic_launcher-playstore.png" width=10%, height=10%)>

- Subscribe to Android OSS repositories (GitHub and GitLab)
- Compare versions with what you have installed on the device
- Opens new version in browser for you to install yourself
- Bulk Import and Export of repo list

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.jroddev.android_oss_release_tracker/)

or get the APK from the [Releases Section](https://github.com/jroddev/android-oss-release-tracker/releases/latest).

---

### WARNING
*This application uses the `QUERY_ALL_PACKAGES` permission in order to fetch the currently installed version of each app in the tracker*
   
See [RenderItem](https://github.com/jroddev/android-oss-release-tracker/blob/master/app/src/main/java/com/jroddev/android_oss_release_tracker/ui/AppsScreen.kt#L182)
```
metaData.installedVersion.value = packageManager
            .getInstalledPackages(0)
            .find { it.packageName == metaData.packageName.value }
            ?.versionName ?: "not installed"
```
---

<img src="https://user-images.githubusercontent.com/9654410/200736147-23eece6e-6d94-4f1b-8d08-5b4992eb136b.png" width=30% height=30%)>
<img src="https://user-images.githubusercontent.com/9654410/200736616-c07cf81a-27ba-43d9-8f4d-49179bdea9fc.png" width=30% height=30%)>





