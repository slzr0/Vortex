@file:Suppress("UnstableApiUsage")

plugins {
    id("vortex.android.application")
    id("vortex.android.application.compose")
    id("vortex.android.hilt")
}

android {
    namespace = "com.my.vortex"

    defaultConfig {
        applicationId = "com.my.kizzy"
        minSdk = 28
        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
        }
    }

    packagingOptions.resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}
dependencies {
    testImplementation (libs.junit)
    implementation (projects.domain)
    implementation (projects.theme)
    implementation (projects.featureCrashHandler)
    implementation (projects.featureProfile)
    implementation (projects.featureAbout)
    implementation (projects.featureSettings)
    implementation (projects.featureLogs)
    implementation (projects.featureRpcBase)
    implementation (projects.featureConsoleRpc)
    implementation (projects.featureCustomRpc)
    implementation (projects.common.preference)
    implementation (projects.common.navigation)
    implementation (projects.common.resources)
    implementation (projects.common.components)
    implementation (libs.material.icons.extended)

    implementation (libs.activity.compose)
    implementation (libs.lifecycle.runtime.compose)
    implementation (libs.core.ktx)
    implementation (libs.app.compat)
    implementation (libs.accompanist.navigation.animation)
    implementation (libs.compose.navigation)
    implementation (libs.lifecycle.runtime)
    implementation (libs.lifecycle.viewmodel.compose)
    implementation (libs.kotlinx.coroutine)
    implementation (libs.kotlinx.serialization.json)


    implementation (libs.material3)
    implementation (libs.androidx.material)
    implementation (libs.material3.windows.size)
}