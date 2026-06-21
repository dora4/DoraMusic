pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
//        maven { setUrl("aop_plugin") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android.tools.build") {
                useModule("com.android.tools.build:gradle:8.1.0")
            }
            if (requested.id.namespace == "org.jetbrains.kotlin") {
                val kotlin_version = "2.2.21"
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
            }
            if (requested.id.namespace == "com.google.firebase") {
                useModule("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
            }
//            if (requested.id.namespace == "com.dorachat") {
//                useModule("com.dorachat:dora-aop-plugin:1.0")
//            }
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

