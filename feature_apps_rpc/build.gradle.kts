plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
}

android {
    namespace = "com.my.vortex.feature_apps_rpc"
}

dependencies {
    implementation(projects.featureRpcBase)
    implementation (libs.material.icons.extended)
}