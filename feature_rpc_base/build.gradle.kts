plugins {
    id ("vortex.android.library")
    id ("vortex.android.feature")
    id ("vortex.android.hilt")
}

android {
    namespace = "com.my.vortex.feature_rpc_base"
}

dependencies {
    implementation (libs.blankj.utilcodex)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil)
}