import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.movtery.petlib"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("proguard-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
}

/*
 * Rust 交叉编译：经 cargo-ndk 构建 cdylib 并注入 jniLibs。
 * 需要本机安装 rustup、cargo-ndk（cargo install cargo-ndk），
 * 以及四个 Android target（rustup target add <triple>）。
 */
val rustAbis = mapOf(
    "arm64-v8a" to "aarch64-linux-android",
    "armeabi-v7a" to "armv7-linux-androideabi",
    "x86" to "i686-linux-android",
    "x86_64" to "x86_64-linux-android",
)
val petlibJniLibs = layout.buildDirectory.dir("petlib-jniLibs")

val cargoBuild = tasks.register("cargoBuild") {
    group = "build"
    description = "Cross-compiles the PetLib Rust core (cdylib) for all ABIs via cargo-ndk."
    inputs.dir("rust/src")
    inputs.file("rust/Cargo.toml")
    inputs.file("rust/Cargo.lock")
    outputs.dir(petlibJniLibs)
    doLast {
        fun tool(command: String, args: List<String>): String? = try {
            val process = ProcessBuilder(listOf(command) + args)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0) output.trim() else null
        } catch (_: java.io.IOException) {
            null
        }

        if (tool("cargo", listOf("--version")) == null) {
            throw GradleException(
                "Rust toolchain is required to build PetLib. Install it from https://rustup.rs and retry."
            )
        }
        if (tool("cargo", listOf("ndk", "--version")) == null) {
            throw GradleException(
                "cargo-ndk is required to build PetLib. Install it with: cargo install cargo-ndk"
            )
        }
        val installedTargets = tool("rustup", listOf("target", "list", "--installed")) ?: ""
        val missing = rustAbis.values.filter { triple -> !installedTargets.contains(triple) }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing Rust Android targets for PetLib. Install them with: rustup target add ${missing.joinToString(" ")}"
            )
        }

        val outDir = petlibJniLibs.get().asFile.apply { mkdirs() }
        val command = listOf(
            "cargo", "ndk",
            "--platform", "26",
            "--output-dir", outDir.absolutePath,
        ) + rustAbis.values.flatMap { listOf("--target", it) } +
            listOf("build", "--release")
        val process = ProcessBuilder(command)
            .directory(project.file("rust"))
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        println(output)
        if (exit != 0) {
            throw GradleException("cargo-ndk build failed with exit code $exit.\n$output")
        }
    }
}

androidComponents {
    onVariants { variant ->
        // cargo-ndk 产物目录（build/petlib-jniLibs/<abi>/libpetlib.so）。
        variant.sources.jniLibs?.addStaticSourceDirectory(
            petlibJniLibs.get().asFile.apply { mkdirs() }.absolutePath
        )
    }
}

tasks.named("preBuild") {
    dependsOn(cargoBuild)
}
