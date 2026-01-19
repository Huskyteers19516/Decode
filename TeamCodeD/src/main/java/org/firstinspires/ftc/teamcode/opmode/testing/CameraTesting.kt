package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.utils.Alliance

@Suppress("unused")
val cameraTesting = Mercurial.teleop("Camera Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry;
    val camera = Camera(hardwareMap)
    val drive = Drive(hardwareMap)

    schedule(
        camera.waitForCamera(telemetryM) {
            telemetryM.update(telemetry)
        }
    )

    waitForStart()
    drive.follower.startTeleopDrive()

    schedule(
        loop(
            exec {
                telemetryM.addLine("Camera Testing")

                if (gamepad1.a) {
                    val targetTag =
                        camera.getTargetTag(Alliance.BLUE) ?: camera.getTargetTag(Alliance.RED)
                    if (targetTag == null) {
                        telemetryM.addData("Orienting status", "Not found")
                    } else {
                        telemetryM.addData(
                            "Orienting status", when (drive.orientTowardsAprilTag(targetTag)) {
                                Drive.State.IN_PROGRESS -> "In progress"
                                Drive.State.DONE -> "Done"
                            }
                        )
                    }
                } else {
                    drive.follower.setTeleOpDrive(0.0, 0.0, 0.0)
                }
                drive.periodic(telemetryM)

                try {
                    camera.debugTelemetry(telemetryM)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()

}