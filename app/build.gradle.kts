import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    id("jacoco")
    //id("com.google.gms.google-services")
    alias(libs.plugins.gms)
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
        applicationId = "com.android.gatherly"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["timeout_msec"] = "30000"

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
    sourceSets.getByName("test") {
                java.srcDirs("src/test/java")
                resources.srcDirs("src/test/resources")
            }   // TODO if we want to implement androidTest only tests just like bootcamp


    signingConfigs {
        getByName("debug").apply {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = System.getenv("DEBUG_STORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("DEBUG_KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("DEBUG_KEY_PASSWORD") ?: "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}


// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.gms.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlin.test)
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

    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

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

    // Google Service and Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.auth)

    // Navigation
    implementation(libs.compose.navigation)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")

    // Credential Manager (for Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Networking
    implementation(libs.okhttp)

    // Google Maps services
    implementation(libs.play.services.maps)

    // Testing
    androidTestImplementation(libs.mockk.android)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.kaspresso.allure.support)
    testImplementation(libs.kotlinx.coroutines.test)

    // Profile picture
    implementation("io.coil-kt:coil-compose:2.6.0")
}

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
    doFirst {
        println("JaCoCo execution data files: ${executionData.files}")
        println("JaCoCo class directories: ${classDirectories.files}")
        println("JaCoCo source directories: ${sourceDirectories.files}")
    }
    reports {
        xml.required = true
        html.required = true
        xml.outputLocation = file("${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        html.outputLocation = file("${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/html")
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )
    // JaCoCo needs to match execution data against the same classes used at test runtime.
    // Include both Kotlin classes and Java bytecode to avoid class mismatch errors.
    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val javacDebugTree = fileTree("${project.layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }


    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree, javacDebugTree))
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