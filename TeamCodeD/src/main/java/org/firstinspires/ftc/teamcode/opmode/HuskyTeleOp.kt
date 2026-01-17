package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.Fiber
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutex
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutexes
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Flippers.Flipper
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.pedroPathing.Drawing
import org.firstinspires.ftc.teamcode.utils.Alliance

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
                telemetryM.update()
            })
        )
    )

    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)
    val drive = Drive(hardwareMap)

    //#endregion

    waitForStart()

    // Drive controls

    bindSpawn(
        risingEdge { gamepad1.left_bumper },
        exec { drive.throttle = 0.5 }
    )

    bindSpawn(
        risingEdge { !gamepad1.left_bumper },
        exec { drive.throttle = 1.0 }
    )

    bindSpawn(
        risingEdge { gamepad1.a },
        exec { drive.isRobotCentric = !drive.isRobotCentric }
    )

    bindSpawn(
        risingEdge { gamepad1.start },
        exec { drive.resetOrientation() }
    )

    var isLaunching = false;

    val prioritiser = Mutexes.Prioritiser<Int> { new, old -> new >= old }

    val flipperMutex = Mutex(prioritiser, Unit)

    fun generateFlipperSequence(flipper: Flipper) = Mutexes.guard(
        flipperMutex,
        { 0 },
        { _ ->
            sequence(
                exec { outtake.active = true },
                wait { outtake.canShoot() },
                Mutexes.guard(
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
                    { _, k -> k }
                )
            )
        },
        { _, k -> k }
    )


    bindSpawn(
        risingEdge { gamepad1.a },
        generateFlipperSequence(Flipper.A)
    )

    bindSpawn(
        risingEdge { gamepad1.b },
        generateFlipperSequence(Flipper.B)
    )

    bindSpawn(
        risingEdge { gamepad1.x },
        generateFlipperSequence(Flipper.C)
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
            outtake.targetVelocity += 100
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_down
        }, exec {
            outtake.targetVelocity -= 100
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_right
        }, exec {
            outtake.targetVelocity += 20
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_left
        }, exec {
            outtake.targetVelocity -= 20
        }
    )

    drive.follower.startTeleopDrive()


    // Main loop
    schedule(
        loop(exec {
            intake.manualPeriodic(gamepad1.right_trigger.toDouble(), telemetryM)
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)
            drive.manualPeriodic(
                -gamepad1.left_stick_y.toDouble(),
                -gamepad1.left_stick_x.toDouble(),
                -gamepad1.right_stick_x.toDouble(),
                telemetryM
            )

            telemetry.addData("Is Launching", isLaunching)
            telemetryM.update(telemetry)
        })
    )

    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}