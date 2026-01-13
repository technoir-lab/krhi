pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    plugins {
        val conventionPluginsVersion = "v46"
        id("io.technoirlab.conventions.kotlin-multiplatform-application") version conventionPluginsVersion
        id("io.technoirlab.conventions.kotlin-multiplatform-library") version conventionPluginsVersion
        id("io.technoirlab.conventions.root") version conventionPluginsVersion
        id("io.technoirlab.conventions.settings") version conventionPluginsVersion
    }
}

plugins {
    id("io.technoirlab.conventions.kotlin-multiplatform-application") apply false
    id("io.technoirlab.conventions.kotlin-multiplatform-library") apply false
    id("io.technoirlab.conventions.root") apply false
    id("io.technoirlab.conventions.settings")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
}

globalSettings {
    projectId = "krhi"

    metadata {
        description = "Render Hardware Interface for Kotlin Multiplatform."
        developer(name = "technoir", email = "technoir.dev@gmail.com")
        license(name = "The Apache Software License, Version 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.txt")
    }
}

include(":core")
include(":backend:mock")
include(":backend:vulkan")
include(":backend:webgpu")
include(":sample")
