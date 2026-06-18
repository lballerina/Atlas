import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("io.coil-kt:coil-compose:2.5.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
            implementation("org.osmdroid:osmdroid-android:6.1.16")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // Supabase
            implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.3")
            implementation("io.github.jan-tennert.supabase:auth-kt:3.0.3")
            implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.3")
            implementation("io.github.jan-tennert.supabase:storage-kt:3.0.3")

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("io.ktor:ktor-client-cio:3.0.3")
        }
        val androidUnitTest by getting {
            dependencies {
                // Compose UI test rule + assertions (JUnit4 flavour)
                implementation("androidx.compose.ui:ui-test-junit4:1.7.8")
                // Needed at runtime — provides the headless Compose engine
                implementation("androidx.compose.ui:ui-test-manifest:1.7.8")
                // Robolectric — runs Android code on the JVM, no emulator needed
                implementation("org.robolectric:robolectric:4.13")
                // Standard JUnit4 runner
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    namespace = "ca.uwaterloo.atlas"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ca.uwaterloo.atlas"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL",
            "\"${project.findProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY",
            "\"${project.findProperty("SUPABASE_KEY")}\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.systemProperty(
                    "robolectric.manifest",
                    "${projectDir}/src/androidMain/AndroidManifest.xml"
                )
                test.systemProperty(
                    "robolectric.sdk",
                    "34"
                )
            }
        }
    }
    sourceSets {
        getByName("test") {
            resources.srcDirs("src/androidUnitTest/resources")
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "ca.uwaterloo.atlas.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ca.uwaterloo.atlas"
            packageVersion = "1.0.0"
        }
    }
}
