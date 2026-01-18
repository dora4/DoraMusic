plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.dorachat.auth"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.github.dora4:dora:1.3.57")
    implementation("com.github.dora4:dora-arouter-support:1.6")
    implementation("com.github.dora4:dora-walletconnect-support:2.1.34") {
        exclude(group = "com.madgag.spongycastle", module = "core")
    }
    implementation("com.github.dora4:dcache-android:3.6.3")
    implementation("com.github.dora4:dview-loading-dialog:1.5")
    kapt("com.alibaba:arouter-compiler:1.5.2")
}