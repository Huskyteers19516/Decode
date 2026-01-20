package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.ifHuh
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.match
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
        risingEdge { gamepad2.start },
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
                            jumpScope {
                                sequence(
                                    exec {
                                        isLaunching = true
                                        try {
                                            flippers.raiseFlipper(flipper)
                                        } catch (e: Exception) {
                                            isLaunching = false
                                            Log.e(TAG, "Failed to raise flipper", e)
                                            jump()
                                        }
                                    },
                                    wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                    exec { flippers.lowerFlipper(flipper) },

                                    wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                    exec { isLaunching = false }
                                )
                            }
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

    var trustingColorSensors = true

    bindSpawn(
        risingEdge {
            gamepad2.y
        },
        exec {
            trustingColorSensors = !trustingColorSensors
        }
    )


    val fiberA = bindSpawn(
        risingEdge { gamepad1.a },
        ifHuh(
            { trustingColorSensors },
            match { colorSensors.getBestSlot(ColorSensors.Companion.Artifact.GREEN) }
                .branch(
                    Slot.A, generateFlipperSequence(Slot.A)
                )
                .branch(Slot.B, generateFlipperSequence(Slot.B))
                .branch(Slot.C, generateFlipperSequence(Slot.C))
        ).elseHuh(generateFlipperSequence(Slot.A))
    )

    val fiberB = bindSpawn(
        risingEdge { gamepad1.b },
        ifHuh(
            { trustingColorSensors },
            match { colorSensors.getBestSlot(ColorSensors.Companion.Artifact.GREEN) }
                .branch(
                    Slot.A, generateFlipperSequence(Slot.A)
                )
                .branch(Slot.B, generateFlipperSequence(Slot.B))
                .branch(Slot.C, generateFlipperSequence(Slot.C))
        ).elseHuh(generateFlipperSequence(Slot.B))
    )

    val fiberC = bindSpawn(
        risingEdge { gamepad1.x && !trustingColorSensors },
        generateFlipperSequence(Slot.C)
    )

    bindSpawn(
        risingEdge {
            gamepad2.left_bumper
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


    // TODO: Find a better way to do the manual overrides
    bindSpawn(
        risingEdge {
            gamepad2.a
        },
        exec {
            try {
                flippers.raiseFlipper(Slot.A)
            } catch (e: Exception) {
                Log.e(TAG, "Gamepad 2 manual override failed to raise flipper", e)
            }
        }
    )

    bindSpawn(
        risingEdge {
            !gamepad2.a
        },
        exec {
            flippers.lowerFlipper(Slot.A)
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.b
        },
        exec {
            try {
                flippers.raiseFlipper(Slot.B)
            } catch (e: Exception) {
                Log.e(TAG, "Gamepad 2 manual override failed to raise flipper", e)
            }
        }
    )

    bindSpawn(
        risingEdge {
            !gamepad2.b
        },
        exec {
            flippers.lowerFlipper(Slot.B)
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.x
        },
        exec {
            try {
                flippers.raiseFlipper(Slot.C)
            } catch (e: Exception) {
                Log.e(TAG, "Gamepad 2 manual override failed to raise flipper", e)
            }
        }
    )

    bindSpawn(
        risingEdge {
            !gamepad2.x
        },
        exec {
            flippers.lowerFlipper(Slot.C)
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
            telemetryM.addLine("(Gamepad 1) Slow down: left bumper, reset orientation: start")
            telemetryM.addLine("(Gamepad 2) Change drive mode: start")
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
            telemetryM.addLine((if (!trustingColorSensors) "NOT " else "") + "Trusting color sensors (Y Gamepad 2)")
            val sensorLoopTime = measureTime {
                if (loops % TeleOpConstants.COLOR_SENSOR_INTERVAL == 0) {
                    colorSensors.update()
                }
                colorSensors.telemetry(telemetryM)
            }
            totalSensorLoopTime += sensorLoopTime

            telemetryM.hl()
            telemetryM.addLine("(Gamepad 2) Start/stop outtake: left bumper, control velocity: dpad")

            val outtakeLoopTime = measureTime {
                outtake.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)
            }

            totalOuttakeLoopTime += outtakeLoopTime
            telemetryM.hl()

            if (trustingColorSensors) {
                telemetryM.addLine("(Gamepad 1) A for GREEN flipper, B for PURPLE flipper")
            } else {
                telemetryM.addLine("(Gamepad 1 & 2) A for A flipper, B for B flipper, X for C flipper")
            }
            val flippersLoopTime = measureTime {
                flippers.periodic(telemetryM, TeleOpConstants.DEBUG_MODE)
            }
            totalFlippersLoopTime += flippersLoopTime

            telemetryM.hl()

            telemetryM.addLine("(Gamepad 1) Right trigger for intake in, left trigger for intake out")
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