plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace ="com.webook.app"// "WEbook"//""com.example.helloandroid"
    compileSdk = 35

    defaultConfig {
        applicationId ="com.webook.app"//"WEbook"// "com.example.helloandroid"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "loginkeep"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/daydream/Library/Mobile Documents/com~apple~CloudDocs/무제")
            storePassword = "ecclesia1517" // 여기에 실제 비밀번호
            keyAlias = "key0"           // 여기에 실제 키 alias
            keyPassword = "ecclesia1517"     // 여기에 실제 키 비밀번호
        }
    }



    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation("io.coil-kt:coil-compose:2.4.0")
    testImplementation(libs.junit)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}