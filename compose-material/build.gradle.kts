/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    alias(libs.plugins.dokka)
    alias(libs.plugins.metalavaGradle)
    alias(libs.plugins.dependencyAnalysis)
    kotlin("android")
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isPseudoLocalesEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
        freeCompilerArgs += listOf(
            "-opt-in=com.google.android.horologist.annotations.ExperimentalHorologistApi",
        )
    }

    packaging {
        resources {
            excludes +=
                listOf(
                    "/META-INF/AL2.0",
                    "/META-INF/LGPL2.1",
                )
        }
    }

    sourceSets.getByName("main") {
        assets.srcDir("src/main/assets")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }
    lint {
        checkReleaseBuilds = false
        textReport = true
    }
    namespace = "com.google.android.horologist.compose.material"
}

project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
    if (!this.name.endsWith("TestKotlin") && !this.name.startsWith("compileDebug")) {
        compilerOptions {
            freeCompilerArgs.add("-Xexplicit-api=strict")
        }
    }
}

metalava {
    excludedSourceSets.setFrom("src/debug/java")
    filename.set("api/current.api")
}

dependencies {
    api(projects.annotations)
    api(projects.composeLayout)
    api(projects.images.base)

    implementation(platform(libs.compose.bom))
    api(libs.compose.foundation.foundation)
    api(libs.compose.foundation.foundation.layout)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.wearcompose.material)
    api(libs.wearcompose.foundation)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.annotation)
    implementation(libs.compose.material.iconscore)
    implementation(libs.compose.ui.text)
    implementation(libs.compose.ui.unit)
    implementation(libs.compose.ui.util)

    debugApi(projects.composeTools)
    debugApi(libs.wearcompose.tooling)

    debugImplementation(libs.compose.material.iconsext)
    debugImplementation(libs.compose.ui.toolingpreview)
    debugRuntimeOnly(libs.compose.ui.tooling)
    debugRuntimeOnly(libs.compose.ui.test.manifest)

    testImplementation(projects.roboscreenshots)
    testImplementation(libs.androidx.core)
    testImplementation(libs.compose.material.iconsext)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testRuntimeOnly(libs.compose.ui.test.manifest)
    testImplementation(projects.images.coil)
}

dependencyAnalysis {
    issues {
        onAny {
            severity("fail")
            exclude(":annotations") // bug: reported as unused
        }
    }
}

apply(plugin = "com.vanniktech.maven.publish")
