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

rootProject.name = "FarmAI"

include(":app")
include(":core:domain")
include(":core:data")
include(":feature:farmer")
include(":feature:broker")
include(":feature:receipt")