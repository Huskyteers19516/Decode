package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.hardware.ColorSensors

@Suppress("unused")
val colorSensorTesting = Mercurial.teleop("Color Sensor Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry;
    val colorSensors = ColorSensors(hardwareMap)

    waitForStart()
    schedule(
        loop(
            exec {
                telemetryM.addLine("Color Sensor Testing")
                colorSensors.debugTelemetry(telemetryM)
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}