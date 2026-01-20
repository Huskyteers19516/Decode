plugins {
    id("dev.frozenmilk.android-library") version "11.0.0-1.1.0"
}

android.namespace = "dev.frozenmilk.dairy"

// Most FTC libraries will want the following
ftc {
    kotlin() // if you don't want to use kotlin, remove this

    sdk {
        implementation(RobotCore)
        implementation(FtcCommon)
    }
}

repositories {
    maven("https://repo.dairy.foundation/releases")
}

dependencies {
    api("dev.frozenmilk.sinister:Sloth:0.2.4")
    api(project(":Mercurial"))
    api("org.jetbrains.kotlin:kotlin-reflect")
}
