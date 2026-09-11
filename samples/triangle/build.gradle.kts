@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("io.technoirlab.conventions.kotlin-multiplatform-application")
}

kotlinMultiplatformApplication {
    packageName = "io.technoirlab.rhi.samples.triangle"
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
    }
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":samples:common"))
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
