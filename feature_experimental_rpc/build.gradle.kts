plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.hilt")
    id ("vortex.android.feature")
}

android {
    namespace = "com.my.vortex.feature_experimental_rpc"
}

dependencies {
    implementation(libs.material.icons.extended)
    implementation(libs.activity.compose)
    implementation(libs.blankj.utilcodex)
    implementation(libs.coil)
    implementation(projects.featureRpcBase)
}
