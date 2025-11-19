plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.project" // Your app's namespace
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

    dependencies {
        // If using version catalog, the libs variable should be defined in libs.versions.toml
        dependencies {
            implementation(libs.appcompat)
            implementation(libs.material)
            implementation(libs.constraintlayout)
            implementation(libs.lifecycle.livedata.ktx)
            implementation ("com.google.android.material:material:1.11.0")
            implementation ("com.squareup.picasso:picasso:2.71828")
            implementation ("androidx.annotation:annotation:1.7.1")
            implementation(libs.lifecycle.viewmodel.ktx)
            implementation(libs.navigation.fragment)
            implementation(libs.navigation.ui)
            
            // QR/Barcode Scanning
            implementation("com.google.zxing:core:3.5.1")
            implementation(libs.zxing)
            
            // OCR
            implementation("com.google.mlkit:text-recognition:16.0.0")
            
            // Excel handling
            implementation("org.apache.poi:poi:5.2.3")
            implementation("org.apache.poi:poi-ooxml:5.2.3")
            
            // Database
            implementation("androidx.room:room-runtime:2.5.2")
            annotationProcessor("androidx.room:room-compiler:2.5.2")
            
            // File handling
            implementation("androidx.documentfile:documentfile:1.0.1")
            
            // RecyclerView
            implementation("androidx.recyclerview:recyclerview:1.3.1")
            
            testImplementation(libs.junit)
            androidTestImplementation(libs.ext.junit)
            androidTestImplementation(libs.espresso.core)
    }

}

