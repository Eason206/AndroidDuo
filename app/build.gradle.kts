plugins { id("com.android.application") }

android { namespace = "com.example.foldpoc"; compileSdk = 37
    defaultConfig { applicationId = "com.example.foldpoc"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.window:window:1.5.0")
}
