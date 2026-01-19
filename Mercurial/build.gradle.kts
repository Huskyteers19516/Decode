plugins {
    id("dev.frozenmilk.jvm-library") version "10.3.0-0.1.4"
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

