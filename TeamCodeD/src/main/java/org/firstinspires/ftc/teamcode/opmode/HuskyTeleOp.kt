package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.DriveConstants
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.LoopTimer
import org.firstinspires.ftc.teamcode.utils.hl
import kotlin.time.measureTime

const val TAG = "HuskyTeleOp"

@Suppress("UNUSED")
fun createHuskyTeleOp(startPose: Pose, startAlliance: Alliance) = Mercurial.Program {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry
    var alliance = startAlliance

    schedule(
        deadline(
            wait {
                inLoop
            },
            loop(exec {
                telemetryM.addData("Status", "Initialized")
                telemetryM.addLine("Press B for red, press X for blue")
                telemetryM.addData("Current alliance", alliance)
                if (gamepad1.b) {
                    alliance = Alliance.RED
                } else if (gamepad1.x) {
                    alliance = Alliance.BLUE
                }
                telemetryM.update(telemetry)
            })
        )
    )

    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val drive = Drive(hardwareMap)
    val camera = Camera(hardwareMap)
    drive.follower.setStartingPose(startPose)

    //#endregion

    waitForStart()
    val loopTimer = LoopTimer()
    val isLaunching = false

    schedule(
        loop(
            exec {
                loopTimer.start()
            }
        )
    )

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
        risingEdge { gamepad2.start },
        exec { drive.isRobotCentric = !drive.isRobotCentric }
    )

    bindSpawn(
        risingEdge { gamepad1.start },
        exec { drive.resetOrientation() }
    )


    //#region Velocity adjustment factors

    bindSpawn(
        risingEdge {
            gamepad2.dpad_up
        }, exec {
            outtake.velocityAdjustmentFactor += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_down
        }, exec {
            outtake.velocityAdjustmentFactor -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_right
        }, exec {
            outtake.velocityAdjustmentFactor += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_left
        }, exec {
            outtake.velocityAdjustmentFactor -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )
    //#endregion

    drive.follower.startTeleopDrive(TeleOpConstants.TELEOP_BRAKE_MODE)


    schedule(
        loop(exec {
            telemetryM.addLine("(Gamepad 1) Slow down: left bumper, reset orientation: start")
            telemetryM.addLine("(Gamepad 2) Change drive mode: start")
            measureTime {
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
            }

            telemetryM.hl()

            telemetryM.addData("Is Launching", isLaunching)

            telemetryM.addLine("(Gamepad 2) Start/stop outtake: left bumper, control velocity: dpad")

            loopTimer.section("Outtake") {
                outtake.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)
            }

            telemetryM.hl()

            telemetryM.addLine("(Gamepad 1) Right trigger for intake in, left trigger for intake out")
            loopTimer.section("Intake") {
                intake.manualPeriodic(gamepad1.right_trigger.toDouble(), telemetryM)
            }

            telemetryM.hl()
            loopTimer.end(telemetryM)
            telemetryM.update(telemetry)
        })
    )

    Log.i(TAG, "HuskyTeleOp started")
    dropToScheduler()
}

val HuskyTeleOp = Mercurial.teleop("Husky TeleOp", "Huskyteers", createHuskyTeleOp(Pose(), Alliance.RED))
