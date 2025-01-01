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
        // 蒲公英的仓库
        maven {
            // GitHub CI/CD 环境对 Cookie 的管理可能会更加严格，当域名不合法时会直接拒绝使用。该仓库没有遵循
            // RFC 6265 标准，所以要允许不安全的协议
            isAllowInsecureProtocol = true
            setUrl("https://frontjs-static.pgyer.com/dist/sdk/pgyersdk")
        }
    }
}
rootProject.name = "DoraMusic"
include(":app")

