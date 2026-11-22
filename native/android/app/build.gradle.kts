plugins { id("com.android.application") }

android {
  namespace="com.orbis.sevennote"
  compileSdk=36
  defaultConfig {
    applicationId="com.orbis.sevennote"
    minSdk=24
    targetSdk=35
    versionCode=3
    versionName="1.4.0"
  }
  buildFeatures { buildConfig=true }
  flavorDimensions+="device"
  productFlavors {
    create("mobile") {
      dimension="device"
      targetSdk=36
      manifestPlaceholders["launcherCategory"]="android.intent.category.LAUNCHER"
      manifestPlaceholders["orientation"]="unspecified"
    }
    create("tv") {
      dimension="device"
      targetSdk=35
      applicationIdSuffix=".tv"
      manifestPlaceholders["launcherCategory"]="android.intent.category.LAUNCHER"
      manifestPlaceholders["orientation"]="landscape"
    }
  }
  buildTypes { getByName("release") { isMinifyEnabled=false } }
  compileOptions {
    sourceCompatibility=JavaVersion.VERSION_17
    targetCompatibility=JavaVersion.VERSION_17
  }
  lint { abortOnError=true }
}
dependencies {
  implementation("androidx.webkit:webkit:1.12.1")
  implementation("androidx.activity:activity:1.10.1")
}
