package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
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

    val follower = Constants.createFollower(hardwareMap)

    var alliance = Alliance.RED
    schedule(
        deadline(
            wait {
                inLoop
            },
            loop(exec {
                telemetry.addData("Status", "Initialized")
                telemetry.addLine("Press B for red, press X for blue")
                telemetry.addData("Current alliance", alliance)
                if (gamepad1.b) {
                    alliance = Alliance.RED
                } else if (gamepad1.x) {
                    alliance = Alliance.BLUE
                }
                telemetry.update()
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


    // Main loop
    schedule(
        loop(exec {
            intake.periodic()
            outtake.periodic()
            flippers.periodic()


            follower.update()
            follower.setTeleOpDrive(
                -gamepad1.left_stick_y.toDouble() * throttle,
                -gamepad1.left_stick_x.toDouble() * throttle,
                -gamepad1.right_stick_x.toDouble() * throttle,
                isRobotCentric
            )
            telemetry.addData("X (in)", follower.pose.x)
            telemetry.addData("Y (in)", follower.pose.y)
            telemetry.addData("Heading (deg)", Math.toDegrees(follower.pose.heading))
            telemetry.addData("Throttle", throttle)
            telemetry.addData(
                "Drive mode",
                if (isRobotCentric) "Robot centric" else "Field centric"
            )
            Drawing.drawDebug(follower)
            telemetry.update()
        })
    )

    var isLaunching = false

    fun generateFlipperSequence(flipper: Flipper) = sequence(
        exec {
            isLaunching = true
            outtake.start()
        },
        wait { outtake.canShoot() },
        exec {
            flippers.raiseFlipper(flipper)
        },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
        exec {
            flippers.lowerFlipper(flipper)
        },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
        exec {
            isLaunching = false
        }
    )

    bindSpawn(
        risingEdge {
            gamepad1.a && !isLaunching
        }, generateFlipperSequence(Flipper.A)
    )
    bindSpawn(
        risingEdge {
            gamepad1.b && !isLaunching
        }, generateFlipperSequence(Flipper.B)
    )
    bindSpawn(
        risingEdge {
            gamepad1.x && !isLaunching
        }, generateFlipperSequence(Flipper.C)
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
            gamepad1.left_bumper
        },
        exec {
            intake.start()
        }
    )
    bindSpawn(
        risingEdge {
            !gamepad1.left_bumper
        },
        exec {
            intake.stop()
        }
    )

    follower.startTeleOpDrive()


    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}