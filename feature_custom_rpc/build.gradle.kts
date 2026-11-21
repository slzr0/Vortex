plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
    id ("vortex.android.hilt")
}

android {
    namespace = "com.my.vortex.feature_custom_rpc"
}

dependencies {
    implementation (libs.material.icons.extended)
    implementation(libs.accompanist.permission)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.blankj.utilcodex)
    implementation(libs.coil)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.featureRpcBase)
    implementation(projects.featureProfile)
}