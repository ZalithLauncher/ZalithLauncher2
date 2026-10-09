import com.android.build.api.variant.FilterConfiguration.FilterType.ABI
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URL
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("com.movtery.buildkeys")
}

val launcherNamespace = "com.movtery.zalithlauncher"
val launcherApplicationId = "com.entitybrian69.mirailauncher"
val launcherAPPName = project.findProperty("launcher_app_name") as? String ?: error("The \"launcher_app_name\" property is not set in gradle.properties.")
val launcherName = project.findProperty("launcher_name") as? String ?: error("The \"launcher_name\" property is not set in gradle.properties.")
val launcherShortName = project.findProperty("launcher_short_name") as? String ?: error("The \"launcher_short_name\" property is not set in gradle.properties.")
val launcherUrl = project.findProperty("url_home") as? String ?: error("The \"url_home\" property is not set in gradle.properties.")

val launcherVersionCode = (project.findProperty("launcher_version_code") as? String)?.toIntOrNull() ?: error("The \"launcher_version_code\" property is not set as an integer in gradle.properties.")
val launcherVersionName = project.findProperty("launcher_version_name") as? String ?: error("The \"launcher_version_name\" property is not set in gradle.properties.")

val defaultOAuthClientID = project.findProperty("oauth_client_id") as? String
val defaultStorePassword = project.findProperty("default_store_password") as? String ?: error("The \"default_store_password\" property is not set in gradle.properties.")
val defaultKeyPassword = project.findProperty("default_key_password") as? String ?: error("The \"default_key_password\" property is not set in gradle.properties.")
val defaultCurseForgeApiKey = project.findProperty("curseforge_api_key") as? String

val projectArch: String = System.getProperty("arch", "all")

// Signing: prefer CI-injected env vars (KEYSTORE_FILE, KEYSTORE_PASSWORD,
// KEYSTORE_KEY_PASSWORD, KEYSTORE_ALIAS); fall back to the bundled jks for local dev.
val ciKeystorePath    = System.getenv("KEYSTORE_FILE").takeIf { !it.isNullOrBlank() }
val ciStorePassword   = System.getenv("KEYSTORE_PASSWORD")
val ciKeyPassword     = System.getenv("KEYSTORE_KEY_PASSWORD")
val ciKeyAlias        = System.getenv("KEYSTORE_ALIAS")

val signingKeystore   = if (ciKeystorePath != null) file(ciKeystorePath) else file("zalith_launcher_debug.jks")
val signingStorePass  = ciStorePassword  ?: defaultStorePassword
val signingKeyPass    = ciKeyPassword    ?: ciStorePassword ?: defaultKeyPassword
val signingAlias      = ciKeyAlias       ?: "movtery_zalith_debug"
// P12/PKCS12 keystores require storeType = "PKCS12"; JKS files use the default ("JKS")
val signingStoreType  = if (ciKeystorePath?.endsWith(".p12", ignoreCase = true) == true) "PKCS12" else "JKS"

fun getKeyFromLocal(envKey: String, fileName: String? = null, default: String? = null): String {
    val key = System.getenv(envKey)
    return key ?: fileName?.let {
        val file = File(rootDir, fileName)
        if (file.canRead() && file.isFile) file.readText() else null
    } ?: default ?: run {
        logger.warn("BUILD: $envKey not set; related features may throw exceptions.")
        ""
    }
}

android {
    namespace = launcherNamespace
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    signingConfigs {
        create("releaseBuild") {
            storeFile = signingKeystore
            storeType = signingStoreType
            storePassword = signingStorePass
            keyAlias = signingAlias
            keyPassword = signingKeyPass
        }
        create("debugBuild") {
            storeFile = signingKeystore
            storeType = signingStoreType
            storePassword = signingStorePass
            keyAlias = signingAlias
            keyPassword = signingKeyPass
        }
    }

    defaultConfig {
        applicationId = launcherApplicationId
        applicationIdSuffix = ".v2"
        minSdk = 26
        targetSdk = 34
        versionCode = launcherVersionCode
        versionName = launcherVersionName
        manifestPlaceholders["launcher_name"] = launcherAPPName
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("releaseBuild")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debugBuild")
        }
    }

    splits {
        val arch = projectArch.takeIf { it != "all" } ?: return@splits
        abi {
            isEnable = true
            reset()
            when (arch) {
                "arm" -> include("armeabi-v7a")
                "arm64" -> include("arm64-v8a")
                "x86" -> include("x86")
                "x86_64" -> include("x86_64")
            }
        }
    }

    ndkVersion = "29.0.14206865"

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf("**/libbytehook.so")
        }
    }

    compileOptions {
        // sora-editor language-textmate
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
        prefab = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        ignoreWarnings = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }
                afterEvaluate {
                    val task = tasks.named("merge${variantName}Assets").get() as MergeSourceSetFolders
                    task.inputs.property("lwjglArch", projectArch)
                    task.doLast {
                        val assetsDir = task.outputDir.get().asFile
                        val tag = "JREAssetsCleanup"
                        logger.lifecycle("[$tag] arch: $projectArch")
                        val jreList = listOf("jre-8", "jre-17", "jre-21", "jre-25")
                        jreList.forEach { jreVersion ->
                            val runtimeDir = File("$assetsDir/runtimes/$jreVersion")
                            logger.lifecycle("[$tag] runtimeDir: ${runtimeDir.absolutePath}")
                            runtimeDir.listFiles()?.forEach {
                                if (projectArch != "all" && it.name != "version" && !it.name.contains("universal") && it.name != "bin-$projectArch.tar.xz") {
                                    logger.lifecycle("[$tag] delete: $it : ${it.delete()}")
                                }
                            }
                        }

                        if (projectArch == "all") return@doLast
                        val abi = when (projectArch) {
                            "arm" -> "armeabi-v7a"
                            "arm64" -> "arm64-v8a"
                            "x86" -> "x86"
                            "x86_64" -> "x86_64"
                            else -> return@doLast
                        }
                        val lwjglVersions = file("libs").listFiles { f ->
                            f.name.matches(Regex("lwjgl-\\d+\\.\\d+\\.\\d+-natives-release\\.aar"))
                        }
                            ?.map { Regex("lwjgl-(\\d+\\.\\d+\\.\\d+)-natives-release\\.aar").find(it.name)!!.groupValues[1] }
                            ?: emptyList()
                        lwjglVersions.forEach { version ->
                            val nativesDir = File(assetsDir, "app_runtime/lwjgl/$version/natives")
                            if (nativesDir.isDirectory) {
                                nativesDir.listFiles()?.forEach { dir ->
                                    if (dir.isDirectory && dir.name != abi) {
                                        logger.lifecycle("Removing non-target-arch natives: $dir")
                                        dir.deleteRecursively()
                                    }
                                }
                            }
                        }
                    }
                }

                (output.getFilter(ABI)?.identifier ?: "all").let { abi ->
                    val baseName = "$launcherName-${if (variant.buildType == "release") launcherVersionName else "Debug-$launcherVersionName"}"
                    output.outputFileName = if (abi == "all") "$baseName.apk" else "$baseName-$abi.apk"
                }
            }
        }
    }
}


val mobileGluesLibs by tasks.registering {
    val abis = setOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
    doLast {
        val jniLibsDir = file("src/main/jniLibs")
        val versionFile = file("src/main/jniLibs/.mobileglues_version")

        val expectedLibs = listOf("libMobileGlues.so", "libmobileglues_info_getter.so")
        val allExist = abis.all { abi -> expectedLibs.all { lib -> file("$jniLibsDir/$abi/$lib").exists() } }
        val bundledVersion = if (versionFile.exists()) versionFile.readText().trim() else ""

        // Fetch the latest release metadata so we can version-check.
        val apiUrl = URL("https://api.github.com/repos/MobileGL-Dev/MobileGlues-release/releases/latest")
        val releaseJson = try {
            retryWithBackoff(maxRetries = 3, initialDelayMs = 1000) { attempt ->
                val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.setRequestProperty("Accept", "application/json")
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val body = conn.inputStream.readAllBytes().decodeToString()
                    conn.disconnect()
                    body
                } else {
                    val errorBody = conn.errorStream?.readAllBytes()?.decodeToString() ?: "no body"
                    conn.disconnect()
                    if (responseCode == 403) {
                        logger.warn("MobileGlues API rate limited (attempt $attempt), retrying...")
                        null
                    } else {
                        logger.warn("MobileGlues API request failed (HTTP $responseCode): $errorBody")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to check MobileGlues releases: ${e.message}")
            null
        }

        if (releaseJson == null) {
            if (allExist && bundledVersion.isNotEmpty()) {
                logger.lifecycle("MobileGlues update check failed; using existing bundled $bundledVersion")
                return@doLast
            }
            throw GradleException("MobileGlues API request failed and no valid bundled version is present.")
        }

        val latestTag = Regex("\"tag_name\":\"([^\"]+)\"").find(releaseJson)?.groupValues?.get(1)
            ?: throw GradleException("Could not parse tag_name from MobileGlues release JSON")

        // Skip the download only when ALL expected libraries are present AND the
        // bundled version already matches the latest tag.
        if (allExist && bundledVersion == latestTag) {
            logger.lifecycle("MobileGlues $latestTag is already up-to-date — skipping download")
            return@doLast
        }

        logger.lifecycle("Updating MobileGlues: bundled=$bundledVersion latest=$latestTag")

        val assetUrl = Regex("\"browser_download_url\":\"([^\"]+\\.apk)\"").find(releaseJson)?.groupValues?.get(1)
            ?: throw GradleException("No APK asset found in latest MobileGlues release")

        val apkFile = layout.buildDirectory.file("tmp/mobileglues.apk").get().asFile
        apkFile.parentFile.mkdirs()

        logger.lifecycle("Downloading MobileGlues from $assetUrl")
        URL(assetUrl).openStream().use { input ->
            apkFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // The upstream release APK packages the native library with an
        // all-lowercase filename (lib/<abi>/libmobileglues.so), while the
        // launcher's runtime (LIB_MESA_NAME / DLOPEN) looks for the
        // mixed-case "libMobileGlues.so". Android's storage is
        // case-sensitive, so a naive case-sensitive zip entry lookup never
        // matches and the library silently never gets bundled, causing a
        // "library libMobileGlues.so not found" crash at launch. Resolve
        // the entry case-insensitively and always write the extracted file
        // out using the exact case the runtime expects.
        //
        // libmobileglues_info_getter.so is kept all-lowercase (matching the APK
        // entry name) because nothing in the launcher looks it up by name at
        // runtime — Android's PackageManager loads it automatically via the
        // normal jniLibs merge during packaging.
        ZipFile(apkFile).use { zip ->
            abis.forEach { abi ->
                val outDir = file("$jniLibsDir/$abi")
                outDir.mkdirs()

                listOf(
                    "libmobileglues.so" to "libMobileGlues.so",
                    "libmobileglues_info_getter.so" to "libmobileglues_info_getter.so",
                ).forEach { (apkName, outName) ->
                    val entry = zip.entries().asSequence()
                        .firstOrNull { it.name.equals("lib/$abi/$apkName", ignoreCase = true) }
                    if (entry != null) {
                        zip.getInputStream(entry).use { input ->
                            File(outDir, outName).outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        logger.lifecycle("Extracted lib/$abi/$apkName -> $abi/$outName")
                    } else {
                        logger.warn("lib/$abi/$apkName not found in MobileGlues release APK")
                    }
                }
            }
        }
        apkFile.delete()

        // Record the version we just bundled so future builds can skip the download.
        versionFile.writeText(latestTag)
        logger.lifecycle("MobileGlues $latestTag bundled successfully")
    }
}

fun retryWithBackoff(
    maxRetries: Int,
    initialDelayMs: Long,
    action: (attempt: Int) -> String?
): String? {
    var delay = initialDelayMs
    for (attempt in 1..maxRetries) {
        val result = action(attempt)
        if (result != null) return result
        if (attempt < maxRetries) {
            Thread.sleep(delay)
            delay *= 2
        }
    }
    return null
}

val nativeLibPluginLibs by tasks.registering {
    val abis = setOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
    doLast {
        val jniLibsDir = file("src/main/jniLibs")
        val allExist = abis.all { file("$jniLibsDir/$it/libimgui-java.so").exists() }
        if (allExist) return@doLast

        val apkUrl = "https://github.com/ZalithLauncher/NativeLibPlugin/releases/download/v1.86.12_Patched/app-release.apk"
        val apkFile = layout.buildDirectory.file("tmp/nativelibplugin.apk").get().asFile
        apkFile.parentFile.mkdirs()

        logger.lifecycle("Downloading NativeLibPlugin from $apkUrl")
        URL(apkUrl).openStream().use { input ->
            apkFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        ZipFile(apkFile).use { zip ->
            abis.forEach { abi ->
                val outDir = file("$jniLibsDir/$abi")
                outDir.mkdirs()

                val entry = zip.getEntry("lib/$abi/libimgui-java.so")
                if (entry != null) {
                    zip.getInputStream(entry).use { input ->
                        File(outDir, "libimgui-java.so").outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    logger.lifecycle("Extracted lib/$abi/libimgui-java.so")
                } else {
                    logger.warn("lib/$abi/libimgui-java.so not found in APK")
                }
            }
        }
        apkFile.delete()
    }
}

afterEvaluate {
    tasks.matching { it.name.startsWith("merge") && it.name.endsWith("JniLibs") }.configureEach {
        dependsOn(mobileGluesLibs)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }
}

buildKeys {
    string("OAUTH_CLIENT_ID", getKeyFromLocal("OAUTH_CLIENT_ID", ".oauth_client_id.txt", defaultOAuthClientID), true)
    string("LAUNCHER_NAME", launcherAPPName, true)
    string("LAUNCHER_IDENTIFIER", launcherName, true)
    string("LAUNCHER_SHORT_NAME", launcherShortName, true)
    string("URL_HOME", launcherUrl, true)
    string("CURSEFORGE_API", getKeyFromLocal("CURSEFORGE_API_KEY", ".curseforge_api.txt", defaultCurseForgeApiKey), true)
    string("BUILD_ARCH", projectArch)
}

dependencies {
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-rc01")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-rc01")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.3.0-rc01")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.webkit)
    implementation(libs.documentfile)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.network.ktor3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.material)
    implementation(libs.material.color.utilities)
    implementation(libs.materialKolor)
    implementation(libs.reorderable)
    implementation(libs.richtext.commonmark)
    implementation(libs.richtext.ui)
    implementation(libs.richtext.ui.material3)
    implementation(platform(libs.editor.bom))
    implementation(libs.editor)
    implementation(libs.editor.language.textmate)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    //Project
    implementation(project(":LayerController"))
    implementation(project(":ColorPicker"))
    implementation(project(":CardGrid"))
    implementation(project(":Terracotta"))
    implementation(project(":InputMap"))
    implementation(project(":Guide"))
    implementation(project(":ltw"))
    //Utils
    implementation(libs.bytehook)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.zip4j)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.minidns.hla)
    implementation(libs.toml4j)
    implementation(libs.maven.artifact)
    implementation(libs.mmkv)
    implementation(libs.fishnet)
    implementation(libs.process.phoenix)
    implementation(libs.lunarcalendar)
    implementation(libs.compose.markdown)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    //Safe
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)
    ksp(libs.androidx.room.compiler)
    //Support
    implementation(libs.proxy.client.android)
    //Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    //Test
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver3)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
