@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("io.technoirlab.conventions.kotlin-multiplatform-application")
}

kotlinMultiplatformApplication {
    packageName = "io.technoirlab.rhi.sample"
}

kotlin {
    androidNativeArm64()
    iosArm64()
    iosSimulatorArm64()
    linuxArm64()
    linuxX64()
    macosArm64()
    mingwX64()
    js {
        browser()
        binaries.executable()
    }
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
        }
        nativeMain.dependencies {
            implementation(project(":backend:vulkan"))
        }
        webMain.dependencies {
            implementation(project(":backend:webgpu"))
            implementation(libs.kotlin.browser)
            implementation(libs.kotlin.web)
        }
    }
}
