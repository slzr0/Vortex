plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
    id ("vortex.android.feature")
}

android {
    namespace = "com.my.vortex.feature_crash_handler"
}

dependencies {
    implementation (libs.activity.compose)
    implementation(libs.crashx)
    implementation (libs.blankj.utilcodex)
    implementation (libs.material.icons.extended)
    implementation (projects.theme)
}