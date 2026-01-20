package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutex
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutexes
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.DriveConstants
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.Slot
import org.firstinspires.ftc.teamcode.utils.hl
import kotlin.time.Duration
import kotlin.time.measureTime

const val TAG = "HuskyTeleOp"

@Suppress("UNUSED")
val huskyTeleOp = Mercurial.teleop("HuskyTeleOp", "Huskyteers") {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;


    var alliance = Alliance.RED
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
    val flippers = Flippers(hardwareMap)
    val drive = Drive(hardwareMap)
    val colorSensors = ColorSensors(hardwareMap)

    //#endregion

    waitForStart()

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
        risingEdge { gamepad2.y },
        exec { drive.isRobotCentric = !drive.isRobotCentric }
    )

    bindSpawn(
        risingEdge { gamepad1.start },
        exec { drive.resetOrientation() }
    )

    var isLaunching = false

    val prioritiser = Mutexes.Prioritiser<Int> { new, old -> new >= old }

    val flipperMutex = Mutex(prioritiser, Unit)

    fun generateFlipperSequence(flipper: Slot) =
        Mutexes.guardPoll(
            flipperMutex,
            { 0 },
            { _ ->
                sequence(
                    exec { outtake.active = true },
                    wait { outtake.canShoot() },
                    Mutexes.guardPoll(
                        flipperMutex,
                        { 1 },
                        { _ ->
                            sequence(
                                exec {
                                    isLaunching = true
                                    flippers.raiseFlipper(flipper)
                                },
                                wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                exec { flippers.lowerFlipper(flipper) },

                                wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                exec { isLaunching = false }
                            )
                        },
                        // should be impossible
                        noop(),
                        noop()
                    )
                )
            },
            noop(),
            noop()
        )

    val fiberA = bindSpawn(
        risingEdge { gamepad1.a },
        generateFlipperSequence(Slot.A)
    )

    val fiberB = bindSpawn(
        risingEdge { gamepad1.b },
        generateFlipperSequence(Slot.B)
    )

    val fiberC = bindSpawn(
        risingEdge { gamepad1.x },
        generateFlipperSequence(Slot.C)
    )

    bindSpawn(
        risingEdge {
            gamepad2.a
        }, Mutexes.guardPoll(
            flipperMutex,
            { -1 },
            { _ ->
                exec { outtake.toggle() }
            },
            noop(),
            noop()
        )
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_up
        }, exec {
            outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_down
        }, exec {
            outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_right
        }, exec {
            outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_left
        }, exec {
            outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
        }
    )

    drive.follower.startTeleopDrive(TeleOpConstants.TELEOP_BRAKE_MODE)


    // Main loop
    var loops = 0
    var totalDriveLoopTime = Duration.ZERO
    var totalSensorLoopTime = Duration.ZERO
    var totalOuttakeLoopTime = Duration.ZERO
    var totalFlippersLoopTime = Duration.ZERO
    var totalIntakeLoopTime = Duration.ZERO


    schedule(
        loop(exec {
            val driveLoopTime =
                measureTime {
                    drive.manualPeriodic(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.left_stick_x.toDouble(),
                        -gamepad1.right_stick_x.toDouble(),
                        telemetryM
                    )
                }
            totalDriveLoopTime += driveLoopTime

            telemetryM.hl()

            telemetryM.addData("Is Launching", isLaunching)
            val sensorLoopTime = measureTime {
                if (loops % TeleOpConstants.COLOR_SENSOR_INTERVAL == 0) {
                    colorSensors.update()
                }
                colorSensors.telemetry(telemetryM)
            }
            totalSensorLoopTime += sensorLoopTime

            telemetryM.hl()

            val outtakeLoopTime = measureTime {
                outtake.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)
            }

            totalOuttakeLoopTime += outtakeLoopTime
            telemetryM.hl()

            val flippersLoopTime = measureTime {
                flippers.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)
            }
            totalFlippersLoopTime += flippersLoopTime

            telemetryM.hl()

            val intakeLoopTime = measureTime {
                intake.manualPeriodic(gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble(), telemetryM)
            }
            totalIntakeLoopTime += intakeLoopTime

            loops++

            var maxLoopTime = Duration.ZERO

            telemetryM.addData("Average drive loop time", totalDriveLoopTime / loops)
            telemetryM.addData("Average outtake loop time", totalOuttakeLoopTime / loops)
            telemetryM.addData("Average flippers loop time", totalFlippersLoopTime / loops)
            telemetryM.addData("Average intake loop time", totalIntakeLoopTime / loops)
            telemetryM.addData("Average sensor loop time", totalSensorLoopTime / loops)
            telemetryM.addData(
                "Average total loop time",
                (totalDriveLoopTime + totalOuttakeLoopTime + totalFlippersLoopTime + totalIntakeLoopTime + totalSensorLoopTime) / loops
            )
            maxLoopTime = maxLoopTime.coerceAtLeast(totalDriveLoopTime)
            telemetryM.addData("Max total loop time", maxLoopTime)

            telemetryM.update(telemetry)
        })
    )

    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}