// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2025-2026 DiscMount contributors

plugins {
    id("com.android.library")
}

android {
    namespace = "dev.discmount.app"
    compileSdk = 37

    defaultConfig {
        minSdk = 26          // Android TV / Google TV con Android 8.0 o superior
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
}

dependencies {
    implementation(project(":discmount-core"))
    implementation("org.videolan.android:libvlc-all:3.6.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")   // motor alternativo para HDR (Apache-2.0)
}
