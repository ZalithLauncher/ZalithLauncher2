plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "net.burningtnt.terracotta"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26
    }

    lint {
        targetSdk = 34
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
}

dependencies {
    implementation(libs.androidx.appcompat)
}