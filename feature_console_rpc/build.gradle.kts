plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
    id ("vortex.android.hilt")
}

android {
    namespace = "com.my.vortex.feature_console_rpc"
}

dependencies {
    testImplementation (libs.junit)
    implementation (projects.featureRpcBase)
    implementation (libs.material.icons.extended)
    implementation (libs.coil)
    implementation (libs.activity.compose)
    implementation (libs.kotlinx.serialization.json)
}