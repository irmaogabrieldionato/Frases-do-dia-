plugins {
    id("com.android.application")
}

android {
    namespace = "com.orbis.meudia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.orbis.meudia"
        minSdk = 23
        targetSdk = 35
        versionCode = 5
        versionName = "1.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
