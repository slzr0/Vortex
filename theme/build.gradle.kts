plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
}

android {
    namespace = "com.my.vortex.ui.theme"
}

dependencies {
    implementation (libs.material3)
    implementation (libs.material3.windows.size)
    implementation (projects.domain)
    implementation (projects.common.preference)
    implementation (projects.color)
    implementation (libs.androidx.material)
    implementation (libs.accompanist.systemUiController)
}