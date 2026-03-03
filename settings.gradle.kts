val requiredJdk = JavaVersion.VERSION_21
check(JavaVersion.current() == requiredJdk) {
    "Holive requires JDK 21. Current JDK: ${System.getProperty("java.version")} (${JavaVersion.current()})"
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Holive"
include(":app")
