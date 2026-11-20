plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
}

android {
    namespace = "com.my.vortex.feature_home"
    defaultConfig {
        buildConfigField("String","VERSION_NAME", "\"${libs.versions.version.name.get()}\"")
    }
}

dependencies {
    implementation (libs.accompanist.flowLayout)
    implementation (libs.material.icons.extended)
    implementation (projects.featureRpcBase)
    implementation (projects.featureSettings)
    implementation (projects.common.navigation)
    implementation (libs.coil)
    implementation (libs.activity.compose)
}