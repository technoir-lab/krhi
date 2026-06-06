plugins {
    id("io.technoirlab.conventions.kotlin-multiplatform-library")
}

kotlinMultiplatformLibrary {
    packageName = "io.technoirlab.rhi.vulkan"
}

kotlin {
    androidNativeArm64()
    iosArm64()
    iosSimulatorArm64()
    linuxArm64()
    linuxX64()
    macosArm64()
    mingwX64()

    compilerOptions {
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.kotlin.logging)
            implementation(libs.vulkan.kotlin)
        }
    }
}
