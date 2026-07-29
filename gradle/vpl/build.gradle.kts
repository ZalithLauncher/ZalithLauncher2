import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * ZL2-owned build script for the VerifiedPluginLoad submodule.
 *
 * The submodule ships its own build.gradle.kts, but that one targets FCL's toolchain
 * (AGP 8.13) and applies `org.jetbrains.kotlin.android`. AGP 9 provides Kotlin support
 * itself and hard-fails when that plugin is applied, so the submodule's own script
 * cannot configure under ZL2's AGP 9.2. Keeping the build config here means the
 * submodule stays byte-for-byte upstream and pinned, and ZL2 does not have to carry a
 * patch inside it.
 *
 * Wired up in settings.gradle.kts via project(":VerifiedPluginLoad").buildFileName. The
 * project directory stays the submodule itself, so the default source sets already point
 * at its sources and nothing has to be redirected here.
 *
 * Source of truth for everything below: VerifiedPluginLoad/build.gradle.kts @ 9bda9d9,
 * minus the kotlin.android plugin (AGP 9 built-in), minus maven-publish (VPL is consumed
 * as a project dependency here, never published from this build).
 */

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.vpl.verifiedpluginload"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.bcprov)
    testImplementation(libs.junit)
}
