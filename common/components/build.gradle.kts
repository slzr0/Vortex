plugins {
    id("vortex.android.library")
    id("vortex.android.library.compose")
}

android {
    namespace = "com.my.vortex.ui.components"
}

dependencies {
    implementation (projects.theme)
    implementation (projects.common.preference)
    implementation (libs.material3)
    implementation (projects.color)
    implementation (libs.android.svg)
    implementation (libs.coil)
    implementation (projects.common.resources)
    implementation (libs.material.icons.extended)
    implementation (libs.blankj.utilcodex)
}