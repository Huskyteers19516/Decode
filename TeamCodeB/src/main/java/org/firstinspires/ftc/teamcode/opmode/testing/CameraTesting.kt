package org.firstinspires.ftc.teamcode.opmode.testing

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.hardware.Drive
import com.huskyteers19516.shared.Alliance

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
//    drive.follower.startTeleopDrive()

    bindSpawn(
        risingEdge {
            gamepad1.a
        },
        exec {
            camera.getTargetTag(Alliance.BLUE)?.let { drive.orientTowardsAprilTag(it, false); Log.d("TESTING", "found april tag"); }
        }
    )

    schedule(
        loop(
            exec {
                telemetryM.addLine("Camera Testing")


                drive.periodic(telemetryM)

                try {
                    camera.debugTelemetry(telemetryM)
                } catch (e: Exception) {
                    telemetryM.addLine("Camera error!!")
                    e.printStackTrace()
                }
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()

}