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
        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation (projects.domain)
    implementation (projects.theme)
    implementation (projects.featureStartup)
    implementation (projects.featureCrashHandler)
    implementation (projects.featureProfile)
    implementation (projects.featureAbout)
    implementation (projects.featureSettings)
    implementation (projects.featureLogs)
    implementation (projects.featureRpcBase)
    implementation (projects.featureAppsRpc)
    implementation (projects.featureMediaRpc)
    implementation (projects.featureConsoleRpc)
    implementation (projects.featureCustomRpc)
    implementation (projects.featureExperimentalRpc)
    implementation (projects.featureHome)
    implementation (projects.common.preference)
    implementation (projects.common.navigation)
    implementation (projects.common.resources)
    implementation (libs.material.icons.extended)

    implementation (libs.app.compat)
    implementation (libs.accompanist.navigation.animation)
    implementation (libs.compose.navigation)
    implementation (libs.datastore.preferences)
    implementation (libs.lifecycle.runtime)
    implementation (libs.kotlinx.coroutine)
    androidTestImplementation (libs.androidx.test.ext.junit)
    androidTestImplementation (libs.androidx.test.core)
    androidTestImplementation (libs.junit)
    implementation (libs.kotlinx.serialization.json)


    implementation (libs.material3)
    implementation (libs.androidx.material)
    implementation (libs.material3.windows.size)
}