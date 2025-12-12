plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("org.jetbrains.dokka") version "1.9.10"
}

android {
    namespace = "tg.univlome.epl"
    compileSdk = 35

    defaultConfig {
        applicationId = "tg.univlome.epl"
        minSdk = 24
        targetSdk = 34
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.animation.graphics.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    implementation("org.osmdroid:osmdroid-wms:6.1.16") // Optionnel si besoin de WMS
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16") // Ajout pour ScaleBarOverlay
    implementation("com.squareup.okhttp3:okhttp:4.9.0")  // Pour les requêtes HTTP (GraphHopper)
    implementation(project(":OSMBonusPack")) // Clusterisation Dynamique des marqueurs

    implementation("com.google.android.gms:play-services-location:21.1.0") //exemple
    implementation("com.google.android.gms:play-services-maps:18.2.0") //exemple

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.5.0")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.airbnb.android:lottie:+")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation(kotlin("stdlib"))
}

tasks.dokkaHtml.configure {
    @Suppress("DEPRECATION")
    outputDirectory.set(buildDir.resolve("dokka/html"))
}

@Suppress("DEPRECATION")
val dokkaPdf by tasks.registering(Exec::class) {
    dependsOn("dokkaHtml")
    group = "documentation"
    description = "Génère la documentation PDF depuis HTML avec wkhtmltopdf"

    val htmlPath = buildDir.resolve("dokka/html/index.html")
    val outputPdf = buildDir.resolve("dokka/documentation.pdf")

    commandLine("wkhtmltopdf", htmlPath.absolutePath, outputPdf.absolutePath)
}