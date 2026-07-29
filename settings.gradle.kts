pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ZalithLauncher"
include(":ZalithLauncher")
include(":LWJGL")
include(":LayerController")
include(":ColorPicker")
include(":Terracotta")
include(":VerifiedPluginLoad")
// The submodule's own build script targets FCL's AGP 8 toolchain and applies
// org.jetbrains.kotlin.android, which AGP 9 rejects outright. ZL2 supplies its own build
// script for it so the submodule can stay pinned at an untouched upstream commit.
project(":VerifiedPluginLoad").buildFileName = "../gradle/vpl/build.gradle.kts"
