package org.firstinspires.ftc.teamcode

import android.util.Log
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.utils.Alliance

const val TAG = "HuskyTeleOp"

@Suppress("UNUSED")
val huskyTeleOp = Mercurial.teleop("Husky TeleOp", "Huskyteers") {
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
                telemetry.addAction {
                    println("Curiosity")
                }
                telemetry.update()
            })
        )
    )

    var throttle = 1.0
    var isRobotCentric = false

    // Drive loop
    schedule(
        sequence(
            wait { inLoop },
            exec {
                follower.startTeleOpDrive()
            },
            loop(exec {
                Log.d(TAG, "Drive loop")
                follower.update()
                follower.setTeleOpDrive(
                    -gamepad1.left_stick_y.toDouble() * throttle,
                    -gamepad1.left_stick_x.toDouble() * throttle,
                    -gamepad1.right_stick_x.toDouble() * throttle,
                    isRobotCentric
                )
                telemetry.addData("X", follower.pose.x)
                telemetry.addData("Y", follower.pose.y)
                telemetry.addData("Heading", follower.pose.heading)
                telemetry.addData(
                    "Drive mode",
                    if (isRobotCentric) "Robot centric" else "Field centric"
                )
                telemetry.update()
            })
        )
    )



    bindSpawn(
        risingEdge { gamepad1.right_bumper },
        exec { throttle = 0.5 }
    )

    bindSpawn(
        risingEdge { !gamepad1.right_bumper },
        exec { throttle = 1.0 }
    )

    bindSpawn(
        risingEdge { gamepad1.a },
        exec { isRobotCentric = !isRobotCentric }
    )

    waitForStart()
    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}