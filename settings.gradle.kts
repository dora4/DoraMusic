pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android.tools.build") {
                useModule("com.android.tools.build:gradle:7.1.2")
            }
            if (requested.id.namespace == "com.hujiang.aspectjx") {
                useModule("com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.8")
            }
            if (requested.id.namespace == "com.google.firebase") {
                useModule("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {  setUrl("https://jitpack.io") }
    }
}
rootProject.name = "DoraMusic"
include(":app")

