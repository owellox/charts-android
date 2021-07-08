buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}