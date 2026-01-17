package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.Fiber
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutex
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutexes
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
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

    val follower = Constants.createFollower(hardwareMap)

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

    //#endregion

    waitForStart()

    // Drive controls

    var throttle = 1.0
    var isRobotCentric = false

    bindSpawn(
        risingEdge { gamepad1.left_bumper },
        exec { throttle = 0.5 }
    )

    bindSpawn(
        risingEdge { !gamepad1.left_bumper },
        exec { throttle = 1.0 }
    )

    bindSpawn(
        risingEdge { gamepad1.a },
        exec { isRobotCentric = !isRobotCentric }
    )

    bindSpawn(
        risingEdge { gamepad1.start },
        exec { follower.pose = Pose() }
    )

    var isLaunching = false;

    val prioritiser = Mutexes.Prioritiser<Int> { new, old -> new >= old }

    val flipperMutex = Mutex(prioritiser, Unit)

    fun generateFlipperSequence(flipper: Flipper) = Mutexes.guard(
        flipperMutex,
        { 0 },
        { _ ->
            sequence(
                exec { outtake.start() },
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
            gamepad2.a && !isLaunching
        }, exec {
            outtake.toggle()
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_up
        }, exec {
            outtake.setTargetVelocity(outtake.getTargetVelocity() + 100)
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_down
        }, exec {
            outtake.setTargetVelocity(outtake.getTargetVelocity() - 100)
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_left
        }, exec {
            outtake.setTargetVelocity(outtake.getTargetVelocity() - 20)
        }
    )

    bindSpawn(
        risingEdge {
            gamepad2.dpad_right
        }, exec {
            outtake.setTargetVelocity(outtake.getTargetVelocity() + 20)
        }
    )

    // Main loop
    schedule(
        loop(exec {
            intake.manualPeriodic(gamepad1.right_trigger.toDouble(), telemetryM)
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)


            follower.update()
            follower.setTeleOpDrive(
                -gamepad1.left_stick_y.toDouble() * throttle,
                -gamepad1.left_stick_x.toDouble() * throttle,
                -gamepad1.right_stick_x.toDouble() * throttle,
                isRobotCentric
            )
            telemetryM.addData("X (in)", follower.pose.x)
            telemetryM.addData("Y (in)", follower.pose.y)
            telemetryM.addData("Heading (deg)", Math.toDegrees(follower.pose.heading))
            telemetryM.addData("Throttle", throttle)
            telemetryM.addData("Is Launching", isLaunching)
            telemetryM.addData(
                "Drive mode",
                if (isRobotCentric) "Robot centric" else "Field centric"
            )
            Drawing.drawDebug(follower)
            telemetryM.update(telemetry)
        })
    )


    follower.startTeleOpDrive()


    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}