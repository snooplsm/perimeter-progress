plugins {
    id("com.android.library") version "9.0.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("com.gradleup.nmcp") version "1.4.4" apply false
    id("com.gradleup.nmcp.aggregation") version "1.4.4"
}

val releaseVersion = providers.gradleProperty("VERSION_NAME")
val centralPortalUsername = providers.gradleProperty("centralPortalUsername")
    .orElse(providers.environmentVariable("CENTRAL_PORTAL_USERNAME"))
    .orElse(providers.environmentVariable("CENTRAL_USERNAME"))
val centralPortalPassword = providers.gradleProperty("centralPortalPassword")
    .orElse(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD"))
    .orElse(providers.environmentVariable("CENTRAL_PASSWORD"))

nmcpAggregation {
    centralPortal {
        username.set(centralPortalUsername)
        password.set(centralPortalPassword)
        publishingType.set("USER_MANAGED")
        publicationName.set(releaseVersion.map { "one.adverse:perimeter-progress:$it" })
    }
}

dependencies {
    nmcpAggregation(project(":android"))
}
