plugins {
    id("com.android.application")
}

android {
    namespace = "dev.saathi.kids"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.saathi.kids"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-kids"

        // Hardware optimization for iQOO 15:
        // Qualcomm Snapdragon 8 Elite (SM8850) is 64-bit ONLY.
        // Stripping 32-bit ABIs eliminates binary bloat and guarantees 64-bit Oryon register alignment.
        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))
}
