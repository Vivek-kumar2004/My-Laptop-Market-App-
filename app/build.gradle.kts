import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.mylaptopmarket"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mylaptopmarket"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "app"
    productFlavors {
        create("store") {
            dimension = "app"
            isDefault = true
        }
        create("admin") {
            dimension = "app"
            applicationId = "com.mylaptopmarket.admin"
            versionCode = 1
            versionName = "1.0.0"
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
                ?: error("Release signing is missing. Create keystore.properties and the upload keystore first.")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.google.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.app.update)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
