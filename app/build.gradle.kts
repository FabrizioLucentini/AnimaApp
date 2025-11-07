plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.anima.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.anima.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    // Ensure kapt can resolve JUnit annotations when generating test stubs
    kaptTest("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Ensure test annotations are available to kapt when generating androidTest stubs
    // This helps avoid `error.NonExistentClass` in kapt-generated stubs for androidTest.
    kaptAndroidTest("androidx.test.ext:junit:1.1.5")
    kaptAndroidTest("androidx.test:runner:1.5.2")

    // Room (explicit coordinates)
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")

    // Lifecycle ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // Biometric authentication
    implementation("androidx.biometric:biometric:1.1.0")

    // Encrypted SharedPreferences for secure PIN storage
    implementation("androidx.security:security-crypto:1.0.0")

    // Optional: WorkManager (in case we later want it instead of AlarmManager)
    implementation("androidx.work:work-runtime-ktx:2.8.1")
}

// Add a convenience task to copy/rename the assembled debug APK to anima.apk
// Usage: .\gradlew.bat copyDebugApkToAnima
tasks.register<Copy>("copyDebugApkToAnima") {
    // Ensure the debug APK is built first
    dependsOn("assembleDebug")

    val apkDir = layout.buildDirectory.dir("outputs/apk/debug")
    from(apkDir)
    include("app-debug.apk")
    into(apkDir)
    // Rename the output file
    rename("app-debug.apk", "anima.apk")
}
