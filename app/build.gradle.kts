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

    val code = 49
    val version = "1.5.4"
    defaultConfig {
        applicationId = "site.doramusic.app"
        minSdk = 23
        targetSdk = 34
        versionCode = code
        versionName = version
        buildFeatures {
            dataBinding = true
            aidl = true
            buildConfig = true
        }
    }
    sourceSets {
        getByName("main") {
            jniLibs.setSrcDirs(arrayListOf("src/main/jniLibs"))
        }
    }
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
        pickFirst("**/libovpnexec.so")
    }
    flavorDimensions("app")
    productFlavors {
        // 线上/公测环境
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
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")

    // Dora全家桶
    implementation("com.github.dora4:dora:1.3.14")
    implementation("com.github.dora4:dcache-android:3.4.4")
    implementation("com.github.dora4:dora-arouter-support:1.6")
    implementation("com.github.dora4:dora-pgyer-support:1.8")
    implementation("com.github.dora4:dora-firebase-support:1.13")
    implementation("com.github.dora4:dora-brvah-support:1.3")
    implementation("com.github.dora4:dora-glide-support:1.4")
    implementation("com.github.dora4:dora-walletconnect-support:1.160") {
        exclude(group = "com.madgag.spongycastle", module = "core")
    }

    implementation("com.github.dora4:dview-titlebar:1.37")
    implementation("com.github.dora4:dview-toggle-button:1.5")
    implementation("com.github.dora4:dview-alert-dialog:1.24")
    implementation("com.github.dora4:dview-loading-dialog:1.5")
    implementation("com.github.dora4:dview-colors:1.1")
    implementation("com.github.dora4:dview-skins:1.10")
    implementation("com.github.dora4:dview-bottom-dialog:1.13")
    implementation("com.github.dora4:dview-avatar:1.4")
    implementation("com.github.dora4:dview-flipper-view:1.2")
    implementation("com.github.dora4:dview-flexible-scrollview:1.0")

    // ARouter
    implementation("com.alibaba:arouter-api:1.5.2")
    kapt("com.alibaba:arouter-compiler:1.5.2")

    // AgentWeb
    implementation("com.github.Justson.AgentWeb:agentweb-core:v5.0.0-alpha.1-androidx") // (必选)
//    implementation("com.github.Justson.AgentWeb:agentweb-filechooser:v5.0.0-alpha.1-androidx") // (可选)
//    implementation("com.github.Justson:Downloader:v5.0.0-androidx") // (可选)

    // AspectJ
//    implementation("org.aspectj:aspectjrt:1.9.19")

    // leakcanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")

    // XXPermissions
    implementation("com.github.getActivity:XXPermissions:18.2")

    // banner
    implementation("io.github.youth5201314:banner:2.2.3")

    implementation("com.nimbusds:nimbus-jose-jwt:9.31")
}