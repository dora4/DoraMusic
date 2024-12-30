plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
//    id("com.dorachat.aop")
}

android {
    namespace = "site.doramusic.app"
    compileSdk = 34
    val version = "1.1.3"
    val code = 5
    defaultConfig {
        applicationId = "site.doramusic.app"
        minSdk = 21
        targetSdk = 34
        versionCode = code
        versionName = version
        buildFeatures {
            dataBinding = true
            aidl = true
            buildConfig = true
        }
    }
    flavorDimensions("app")
    productFlavors {
        // 线上/公测环境··
        create("beta") {
            dimension = "app"
            versionNameSuffix = "-beta"
            buildConfigField("String", "APP_VERSION", "\"V$version\"")
        }
        // 内测/预发环境
        create("alpha") {
            dimension = "app"
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha"
            buildConfigField("String", "APP_VERSION", "\"V$version\"")
        }
        // 开发/调试环境
        create("dev") {
            dimension = "app"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "APP_VERSION", "\"V$version\"")
        }
    }
    signingConfigs {
        create("release") {
            storeFile = File(rootDir, "doramusic.jks")
            keyAlias = "key0"
            keyPassword = "123456"
            storePassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }
//    lint {
//        baseline = file("lint-baseline.xml")
//    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

kapt {
    generateStubs = true
    correctErrorTypes = true
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

kotlin {
    jvmToolchain(17)
}

fun libFileTree() : ConfigurableFileTree {
    val map = hashMapOf<String, Any>()
    map["include"] = arrayOf("*.jar", "*.aar")
    map["dir"] = "libs"
    return fileTree(map)
}

dependencies {
    implementation(libFileTree())
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Dora全家桶
    implementation("com.github.dora4:dcache-android:3.1.8")
    implementation("com.github.dora4:dora:1.2.35")
    implementation("com.github.dora4:dora-arouter-support:1.6")
    implementation("com.github.dora4:dora-apollo-support:1.4")
    implementation("com.github.dora4:dora-pgyer-support:1.8")
    implementation("com.github.dora4:dora-firebase-support:1.13")
    implementation("com.github.dora4:dview-toggle-button:1.5")
    implementation("com.github.dora4:dview-alert-dialog:1.18")
    implementation("com.github.dora4:dview-loading-dialog:1.5")
    implementation("com.github.dora4:dview-colors:1.1")
    implementation("com.github.dora4:dview-skins:1.7")
    implementation("com.github.dora4:dview-bottom-dialog:1.13")
    implementation("com.github.dora4:dview-avatar:1.4")
    implementation("com.github.dora4:dview-titlebar:1.37")

    // ARouter
    implementation("com.alibaba:arouter-api:1.5.2")
    kapt("com.alibaba:arouter-compiler:1.5.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.0")

    // AgentWeb
    implementation("com.github.Justson.AgentWeb:agentweb-core:v5.0.0-alpha.1-androidx") // (必选)
    implementation("com.github.Justson.AgentWeb:agentweb-filechooser:v5.0.0-alpha.1-androidx") // (可选)
    implementation("com.github.Justson:Downloader:v5.0.0-androidx") // (可选)

    // AspectJ
//    implementation("org.aspectj:aspectjrt:1.9.19")

    // leakcanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")

    // XXPermissions
    implementation("com.github.getActivity:XXPermissions:18.2")

    // BaseRecyclerViewAdapterHelper
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.6")

    // banner
    implementation("io.github.youth5201314:banner:2.2.2")
}