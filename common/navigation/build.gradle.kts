plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
}

android {
    namespace = "com.my.vortex.navigation"
}

dependencies {
    implementation (libs.compose.ui)
    implementation (libs.compose.navigation)
    implementation (libs.accompanist.navigation.animation)
}