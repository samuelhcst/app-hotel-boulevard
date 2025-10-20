plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.hotelboulevard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hotelboulevard"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // 1. Para Autenticación (login, registro)
    implementation("com.google.firebase:firebase-auth")

    // 2. Para Firestore (la base de datos de roles, habitaciones, etc.)
    implementation("com.google.firebase:firebase-firestore")

    // 3. Para el adaptador de Firestore (sincronización en tiempo real)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // 4. Para cargar imágenes desde URLs (Cloudinary)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.cloudinary:cloudinary-android:2.4.0")
}