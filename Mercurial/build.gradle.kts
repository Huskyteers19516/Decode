plugins {
    id("dev.frozenmilk.jvm-library") version "11.1.0-1.1.2"
}

ftc {
    kotlin()
}

repositories {
    maven {
        name = "dairyReleases"
        url = uri("https://repo.dairy.foundation/releases")
    }
}

dependencies {
    api("dev.frozenmilk.dairy:Util:1.2.2")
    api("dev.frozenmilk:Sinister:2.2.0")
}

