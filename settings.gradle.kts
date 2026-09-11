pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    // Use the already-cached implementation modules directly. This avoids a
    // missing plugin-marker metadata entry in the repaired local Gradle cache.
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> useModule("com.android.tools.build:gradle:${requested.version}")
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "FoldPoc"
include(":app")
