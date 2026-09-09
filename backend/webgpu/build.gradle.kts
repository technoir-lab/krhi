@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("io.technoirlab.conventions.kotlin-multiplatform-library")
}

kotlinMultiplatformLibrary {
    packageName = "io.technoirlab.rhi.webgpu"
}

kotlin {
    js {
        browser()
    }
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.kotlinx.io.core)
        }
        webMain.dependencies {
            implementation(libs.kotlin.browser)
            implementation(libs.kotlin.web)
        }
    }
}
