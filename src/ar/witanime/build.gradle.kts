plugins {
    id('com.android.application')
    id('kotlin-android')
}

apply from: "$rootProject.projectDir/common.gradle"

android {
    namespace 'eu.kanade.tachiyomi.animeextension.ar.witanime'
    defaultConfig {
        applicationId 'eu.kanade.tachiyomi.animeextension.ar.witanime'
        versionCode 1
        versionName '1.0.0'
    }
}

dependencies {
    implementation project(':core')
}
