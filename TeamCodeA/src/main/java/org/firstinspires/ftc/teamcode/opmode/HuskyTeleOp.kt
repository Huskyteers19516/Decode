package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.huskyteers19516.shared.hl
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.DriveConstants
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Feeders
import org.firstinspires.ftc.teamcode.hardware.Outtake


const val TAG = "HuskyTeleOp"

fun createHuskyTeleOp() = Mercurial.Program {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry

    val drive = Drive(hardwareMap)
    val outtake = Outtake(hardwareMap)
    val feeders = Feeders(hardwareMap)
    drive.follower.setStartingPose(
        Pose(
            blackboard.getOrDefault("x", 0.0) as Double,
            blackboard.getOrDefault("y", 0.0) as Double,
            blackboard.getOrDefault("heading", 0.0) as Double
        )
    )
    //#endregion

    waitForStart()

    var isLaunching = false

    // Drive controls

    bindSpawn(
        risingEdge { gamepad1.left_bumper },
        exec { drive.throttle = DriveConstants.SLOW_MODE_SPEED }
    )

    bindSpawn(
        risingEdge { !gamepad1.left_bumper },
        exec { drive.throttle = DriveConstants.NORMAL_MODE_SPEED }
    )

    bindSpawn(
        risingEdge { gamepad1.a },
        exec { drive.isRobotCentric = !drive.isRobotCentric }
    )

    bindSpawn(
        risingEdge { gamepad1.back },
        exec { drive.resetOrientation() }
    )


    //#region Velocity adjustment factors

    bindSpawn(
        risingEdge {
            gamepad1.dpad_up
        }, exec {
            outtake.velocityAdjustmentFactor += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.dpad_down
        }, exec {
            outtake.velocityAdjustmentFactor -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.dpad_right
        }, exec {
            outtake.velocityAdjustmentFactor += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )



    bindSpawn(
        risingEdge {
            gamepad1.dpad_left
        }, exec {
            outtake.velocityAdjustmentFactor -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge { gamepad1.b && !isLaunching },
        exec {
            outtake.toggle()
        }
    )

    bindSpawn(
        risingEdge { gamepad1.right_bumper },
        sequence(
            wait { !isLaunching },
            exec {
                isLaunching = true
                outtake.active = true
                drive.follower.holdPoint(drive.follower.pose)
            },
            wait {
                outtake.canShoot()
            },
            feeders.shoot(),
            exec {
                isLaunching = false
                drive.follower.startTeleopDrive(TeleOpConstants.TELEOP_BRAKE_MODE)
            }
        )
    )

    //#endregion

    drive.follower.startTeleopDrive(TeleOpConstants.TELEOP_BRAKE_MODE)

    schedule(
        loop(exec {
            telemetryM.addLine("(Gamepad 1) Slow down: left bumper, reset orientation: start")
            telemetryM.addLine("(Gamepad 2) Change drive mode: start")
            if (!isLaunching) {
                telemetryM.addData("is busy", drive.follower.isBusy)
                drive.manualPeriodic(
                    -gamepad1.left_stick_y.toDouble() * TeleOpConstants.FORWARD_MULTIPLIER,
                    -gamepad1.left_stick_x.toDouble() * TeleOpConstants.STRAFE_MULTIPLIER,
                    -gamepad1.right_stick_x.toDouble() * TeleOpConstants.TURN_MULTIPLIER,
                    telemetryM
                )
            } else {
                drive.periodic(telemetryM)
            }

            telemetryM.hl()

            telemetryM.addData("Is Launching", isLaunching)

            telemetryM.addLine("(Gamepad 1) Start/stop outtake: B, control velocity: dpad")
            outtake.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)

            telemetryM.update(telemetry)
        })
    )
    Log.i(TAG, "HuskyTeleOp started")
    dropToScheduler()
}

@Suppress("UNUSED")
val HuskyTeleOp = Mercurial.teleop("Husky TeleOp", "Huskyteers", createHuskyTeleOp())
