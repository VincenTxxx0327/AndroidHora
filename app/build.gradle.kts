import org.gradle.api.internal.DocumentationRegistry.BASE_URL

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.union.hora"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.union.hora"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("keyRelease") {
            storeFile = file(if (!System.getenv("CARD_JKS").isNullOrEmpty()) System.getenv("CARD_JKS") else "E:\\IDE\\cardstory_sign.jks")
            storePassword = "Zxc123456@#\$"
            keyAlias = "CardStorySign"
            keyPassword = "Zxc123456@#\$"
        }
        getByName("debug") {
            storeFile = file(if (!System.getenv("CARD_JKS").isNullOrEmpty()) System.getenv("CARD_JKS") else "E:\\IDE\\cardstory_upload.jks")
            storePassword = "Zxc123456"
            keyAlias = "CardStory"
            keyPassword = "Zxc123456"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs["keyRelease"]
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = false
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
            buildConfigField("String", "SIGN_KEY", "\"your_sign_key\"")
            buildConfigField("boolean", "IS_OBFUSCATED", "$isMinifyEnabled")
            // 添加以下配置：为发布版本生成独立的调试符号文件
            ndk {
                // 生成调试符号文件。可选参数：'none', 'gdb', 'dwarf2', 'dwarf3', 'dwarf4', 'dwarf5'
                // 通常使用 'dwarf' 相关格式，'dwarf' 是标准格式。
                debugSymbolLevel = "FULL" // 或者使用 'SYMBOL_TABLE' (体积更小，但信息较少)
            }
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs["debug"]
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = true
            buildConfigField("String", "BASE_URL", "\"https://test-api.example.com/\"")
            buildConfigField("String", "SIGN_KEY", "\"your_test_sign_key\"")
            buildConfigField("boolean", "IS_OBFUSCATED", "$isMinifyEnabled")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.logger)
    implementation(libs.rxjava3)
    implementation(libs.rxjava3.ktx)
    implementation(libs.rxjava3.android)
    implementation(libs.eventbus)
    implementation(libs.longan)
    implementation(libs.longan.design)
    implementation(libs.agentweb.androidx)
    implementation(libs.gson)
    implementation("com.tencent:mmkv:1.3.9")
    // OkHttp3 dependencies
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    // Coroutines dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Compose dependencies
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.google.dagger.hilt)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    kapt(libs.google.dagger.hilt.compiler)
    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}