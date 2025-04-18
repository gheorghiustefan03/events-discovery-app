import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "eu.ase.acs.eventsappui"
    compileSdk = 35

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:deprecation")
    }

    defaultConfig {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if(localPropertiesFile.exists()){
            localProperties.load(localPropertiesFile.inputStream())
        }

        resValue("string", "MAPS_API_KEY", localProperties.getProperty("MAPS_API_KEY"))
        resValue("string", "LOCALHOST", localProperties.getProperty("LOCALHOST"))

        applicationId = "eu.ase.acs.eventsappui"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation(libs.material)
    implementation(libs.core)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.android.image.slider)
    implementation(libs.play.services.maps)
    implementation(libs.gms.play.services.location)
    implementation(libs.android.maps.utils)
    implementation(libs.threetenabp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
