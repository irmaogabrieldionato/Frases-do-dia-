plugins { id("com.android.application") }

android {
  namespace="com.orbis.sevennote"
  compileSdk=35
  defaultConfig {
    applicationId="com.orbis.sevennote"
    minSdk=24
    targetSdk=35
    versionCode=1
    versionName="1.2.0"
  }
  buildFeatures { buildConfig=true }
  flavorDimensions+="device"
  productFlavors {
    create("mobile") {
      dimension="device"
      manifestPlaceholders["launcherCategory"]="android.intent.category.LAUNCHER"
      manifestPlaceholders["orientation"]="unspecified"
    }
    create("tv") {
      dimension="device"
      applicationIdSuffix=".tv"
      manifestPlaceholders["launcherCategory"]="android.intent.category.LEANBACK_LAUNCHER"
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
dependencies { implementation("androidx.webkit:webkit:1.12.1") }
