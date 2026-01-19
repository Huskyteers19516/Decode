package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("unused")
val driveTesting = Mercurial.teleop("Drive Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry;
    val drive = Drive(hardwareMap)


    waitForStart()

    var individualMode = true

    fun boolToPower(a: Boolean) = if (a) 1.0 else 0.0


    bindExec(risingEdge {
        gamepad1.start
    }, exec {
        individualMode = !individualMode
    })



    bindExec(risingEdge {
        gamepad1.dpad_up
    }, exec {
        drive.resetOrientation()
    })

    bindExec(
        risingEdge { gamepad1.dpad_down },
        exec { drive.isRobotCentric = !drive.isRobotCentric }
    )

    bindExec(
        risingEdge { gamepad1.right_bumper },
        exec {
            drive.throttle = 0.5
        }
    )

    bindExec(
        risingEdge { !gamepad1.right_bumper },
        exec {
            drive.throttle = 1.0
        }
    )

    schedule(
        loop(
            exec {
                telemetryM.addLine("Press start to toggle between drive and individual mode")
                if (individualMode) {
                    telemetryM.addLine("Individual mode")
                    telemetryM.addLine("X = front left, Y = front right, B = rear right, A = rear left")
                    telemetryM.addLine("Rotate your controller 45 deg clockwise to visualize")
                    drive.debugPeriodic(
                        boolToPower(gamepad1.x),
                        boolToPower(gamepad1.y),
                        boolToPower(gamepad1.a),
                        boolToPower(gamepad1.b)
                    )
                } else {
                    telemetryM.addLine("Drive mode")
                    telemetryM.addLine("Press dpad up to reset heading")
                    telemetryM.addLine("Press dpad down to change field centric mode")
                    telemetryM.addData("Drive mode", if (drive.isRobotCentric) "Robot centric" else "Field centric")
                    telemetryM.hl()
                    drive.manualPeriodic(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.left_stick_x.toDouble(),
                        -gamepad1.right_stick_x.toDouble(),
                        telemetryM
                    )

                }

                drive.debugTelemetry(telemetryM)
                telemetryM.update(telemetry)
            }
        )
    )

    drive.follower.startTeleopDrive()

    dropToScheduler()
}