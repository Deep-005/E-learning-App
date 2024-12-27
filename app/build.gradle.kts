plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id ("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.courses"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.courses"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    // implementation("androidx.navigation:navigation-safe-args-ktx:2.8.3")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Firebase dependencies
    implementation (platform("com.google.firebase:firebase-bom:33.5.1")) // BOM for Firebase
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-appcheck")
    implementation ("com.google.firebase:firebase-appcheck-debug")
    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation ("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.firebase:firebase-functions-ktx:21.1.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")
    implementation("androidx.databinding:databinding-runtime:8.7.3")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Additional libraries
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.airbnb.android:lottie:6.5.0")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.denzcoskun:ImageSlideShow:0.1.2")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // MarkWon dependencies
    implementation ("io.noties.markwon:core:4.6.2")
    implementation ("io.noties.markwon:ext-tasklist:4.6.2")
    implementation ("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation ("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")

    // RazorPay Integration for Payments
    implementation ("com.razorpay:checkout:1.6.40")

    //imageSlider
    implementation("com.github.denzcoskun:ImageSlideShow:0.1.2")
}