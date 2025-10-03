import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    id("jacoco")
    id("com.google.gms.google-services")
    //alias(libs.plugins.gms)
}

android {
    namespace = "com.android.gatherly"
    compileSdk = 34

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY") ?: ""


    defaultConfig {
        applicationId = "com.github.se.gatherly"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testCoverage {
        jacocoVersion = "0.8.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            isReturnDefaultValues = true
        }
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
    //getByName("test") {
    //            java.srcDirs("src/test/java")
    //            resources.srcDirs("src/test/resources")
    //        }    TODO if we want to implement androidTest only tests just like bootcamp
}


// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {
    // Core - RETIREZ une des deux dépendances core-ktx dupliquées
    implementation(libs.androidx.core.ktx) // Gardez celle-ci
    // implementation(libs.core.ktx) // COMMENTEZ ou SUPPRIMEZ celle-ci
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.test.core.ktx)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.test.manifest)
    implementation(libs.material)
    implementation(libs.compose.material.icons.extended)

    implementation(platform("com.google.firebase:firebase-bom:32.8.0")) // Version plus stable

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.bom)
    testImplementation(libs.androidx.compose.bom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.compose.test.junit)
    testImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // Kaspresso
    androidTestImplementation(libs.kaspresso)
    testImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.compose.support)
    testImplementation(libs.kaspresso.compose.support)

    // Robolectric
    testImplementation(libs.robolectric)

    // Navigation
    implementation(libs.compose.navigation)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Credential Manager - avec exclusions
    implementation(libs.credentials) {
        exclude(group = "com.google.android.play", module = "core")
    }
    implementation(libs.credentials.play.services.auth) {
        exclude(group = "com.google.android.play", module = "core")
    }
    implementation(libs.googleid) {
        exclude(group = "com.google.android.play", module = "core")
    }

    // Networking
    implementation(libs.okhttp)

    // Testing
    androidTestImplementation(libs.mockk.android)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kaspresso.allure.support)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Ajoutez cette résolution strategy
configurations.all {
    resolutionStrategy {
        eachDependency {
            when (requested.group) {
                "com.google.android.play" -> {
                    if (requested.name == "core") {
                        useVersion("1.10.3")
                    }
                }
            }
        }
    }
}

tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}

configurations.forEach { configuration ->
    // Exclude protobuf-lite from all configurations
    // This fixes a fatal exception for tests interacting with Cloud Firestore
    configuration.exclude("com.google.protobuf", "protobuf-lite")
}