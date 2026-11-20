
plugins {
    id ("vortex.android.library")
    id ("vortex.android.library.compose")
}
android {
    namespace = "com.vortex.color"
}
dependencies {
    api(libs.compose.ui)
    api(libs.core.ktx)
    api(libs.material3)
}