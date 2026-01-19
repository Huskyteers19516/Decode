package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.hardware.Camera

@Suppress("unused")
val cameraTesting = Mercurial.teleop("Camera Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry;
    val camera = Camera(hardwareMap)

    schedule(
        camera.waitForCamera(telemetryM) {
            telemetryM.update(telemetry)
        }
    )

    waitForStart()
    schedule(
        loop(
            exec {
                telemetryM.addLine("Camera Testing")
                camera.debugTelemetry(telemetryM)
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()

}