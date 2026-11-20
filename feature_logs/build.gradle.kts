plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
    id ("vortex.android.hilt")
}

android {
    namespace = "com.my.vortex.feature_logs"
}

dependencies {
    implementation (projects.theme)
    implementation(libs.activity.compose)
}