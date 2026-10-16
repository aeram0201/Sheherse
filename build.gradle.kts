// Root buildscript providing Android Gradle plugin
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.2")
    }
}

// Apply plugins in sub-modules


