package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.utils.Slot
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("unused")
val shooterTesting = Mercurial.teleop("Shooter Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry;

    val outtake = Outtake(hardwareMap)

    waitForStart()

    var velocityMode = true

    bindSpawn(
        risingEdge { gamepad1.start }, exec { velocityMode = !velocityMode }
    )


    bindSpawn(
        risingEdge {
            gamepad1.dpad_up
        },
        exec {
            outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.dpad_down
        },
        exec {
            outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.dpad_left
        },
        exec {
            outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.dpad_right
        },
        exec {
            outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.y
        },
        exec {
            outtake.toggle()
        }
    )

    schedule(
        loop(
            exec {
                telemetryM.addLine("System Testing")
                telemetryM.addLine("Use the left stick to control the intake")
                telemetryM.addLine("A for flipper A, B for flipper B, X for flipper C")
                telemetryM.addLine("Press start to toggle between velocity and manual power modes")
                if (velocityMode) {
                    telemetryM.addData(
                        "Outtake mode",
                        "Constant velocity mode. Use dpad to control the outtake velocity, Y to toggle"
                    )
                    telemetryM.hl()
                    outtake.periodic(telemetryM, true)
                } else {
                    telemetryM.addData("Outtake mode", "Manual power mode. Use the right stick to control the outtake")
                    telemetryM.hl()
                    outtake.manualPeriodic(-gamepad1.right_stick_y.toDouble(), telemetryM)
                }
                telemetryM.hl()

                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}