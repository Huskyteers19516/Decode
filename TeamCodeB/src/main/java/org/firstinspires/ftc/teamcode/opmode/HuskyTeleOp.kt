package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.LoopTimer
import com.huskyteers19516.shared.hl
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.DriveConstants
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import kotlin.time.measureTime


const val TAG = "HuskyTeleOp"

private enum class ShootMode {
    ONE_SHOT,
    CONTINUOUS,
}

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
    drive.follower.setStartingPose(startPose)
    val paths = Paths(drive.follower)
    paths.buildPaths(alliance)

    //#endregion

    waitForStart()
    val loopTimer = LoopTimer()
    var isLaunching = false

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
        risingEdge { gamepad1.back },
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

    bindSpawn(
        risingEdge { gamepad1.dpad_down },
        exec {
            outtake.turretAutoAiming = !outtake.turretAutoAiming
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.a
        },
        exec {
            isLaunching = true
        }
    )
    bindSpawn(
        risingEdge {
            !gamepad1.a
        },
        exec {
            isLaunching = false
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.a
        }, exec {
            outtake.shooterActive = !outtake.shooterActive
        }
    )


    //#endregion

    drive.follower.startTeleopDrive(TeleOpConstants.TELEOP_BRAKE_MODE)

    
    schedule(
        loop(exec {
            telemetryM.addLine("(Gamepad 1) Slow down: left bumper, (Gamepad 1) reset orientation: start")
            telemetryM.addLine("(Gamepad 1) Change drive mode: back")
            loopTimer.section("Drive") {
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
            telemetryM.addData("Can Shoot", outtake.canShoot())

            telemetryM.addLine("(Gamepad 2) Y: toggle shoot mode")
            telemetryM.addLine("(Gamepad 2) X: shoot action by current mode")
            telemetryM.addLine("(Gamepad 2) dpad: velocity")
            telemetryM.addLine("(Turret trim, only when NOT auto/manual) G2 LT/RT: +/-0.2, LB/RB: +/-0.05")

            loopTimer.section("Outtake") {
                val turretAngle = Paths.calculateAimHeading(drive.follower.pose, paths.goalLocation)
                val relativeAngle = turretAngle - drive.follower.pose.heading

                if (isLaunching) {
                    outtake.outtakeOpen = outtake.canShoot()
                    outtake.transferActive = true
                } else {
                    outtake.outtakeOpen = false
                    outtake.transferActive = false
                }


                telemetryM.addData("Turret angle", turretAngle)
                telemetryM.addData("Relative turret angle", relativeAngle)
                outtake.periodic(telemetryM, TeleOpConstants.DEBUG_MODE, relativeAngle)
            }

            telemetryM.hl()

            telemetryM.addLine("(Gamepad 1) Right trigger: intake in")
            loopTimer.section("Intake") {
                val intakePower = gamepad1.right_trigger.toDouble()
                intake.manualPeriodic(intakePower, telemetryM)
                outtake.transferActive = true
            }

            telemetryM.hl()
            loopTimer.end(telemetryM)
            telemetryM.update(telemetry)
        })
    )
    Log.i(TAG, "HuskyTeleOp started")
    dropToScheduler()
}

val HuskyTeleOp =
    Mercurial.teleop(
        "Husky TeleOp",
        "Huskyteers",
        createHuskyTeleOp(Pose(72.0, 72.0, Math.toRadians(90.0)), Alliance.RED)
    )
