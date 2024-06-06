import org.gradle.internal.impldep.bsh.commands.dir

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "ru.bratusev.watchproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.bratusev.watchproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("lib/armeabi-v7a/libnative-lib.so")
        exclude("lib/x86_64/libnative-lib.so")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(files("libs/libcomx-0.5.jar"))
    implementation(files("libs/gson-2.2.4.jar"))
    implementation(files("libs/vpbluetooth-1.14.aar"))
    implementation(files("libs/vpprotocol-2.2.91.15.aar"))
    implementation(files("libs/AMap2DMap_6.0.0_AMapSearch_9.4.5_AMapLocation_6.2.0_20221026.jar"))
    implementation(files("libs/JL_Watch_V1.10.0-release.aar"))
    implementation(files("libs/jl_rcsp_V0.5.2-release.aar"))
    implementation(files("libs/jl_bt_ota_V1.9.3-release.aar"))
    implementation(files("libs/BmpConvert_V1.2.1-release.aar"))
    implementation("no.nordicsemi.android.support.v18:scanner:1.4.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    implementation("com.yanzhenjie.recyclerview:support:1.3.2")
    implementation("tech.gujin:toast-util:1.2.0")

}