plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

ext {
    extName = 'Witanime'
    pkgName = 'witanime'
    extClass = '.Witanime'
    extVersionCode = 1
}

apply from: "$rootProject.rootDir/common.gradle"
