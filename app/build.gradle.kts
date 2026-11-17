plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // GOOGLE MAPS - SECRETS PLUGIN
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.misoft.misoftapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.misoft.misoftapp"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// CONFIGURACIÓN DE LA CLAVE DE GOOGLE MAPS
secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // RETROFIT
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // BIOMETRÍA
    implementation("androidx.biometric:biometric:1.1.0")

    // GLIDE
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // GOOGLE MAPS
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    // UBICACIÓN GPS
    implementation("com.google.android.gms:play-services-location:21.3.0")
    //Splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Banner viewpager
    implementation("androidx.viewpager2:viewpager2:1.1.0")


}
