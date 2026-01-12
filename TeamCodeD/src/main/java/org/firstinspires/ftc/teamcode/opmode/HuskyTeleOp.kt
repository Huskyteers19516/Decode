package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.pedropathing.geometry.Pose
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
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

    //#endregion

    waitForStart()

    //#region Drive

    var throttle = 1.0
    var isRobotCentric = false

    schedule(
        sequence(
            wait { inLoop },

            loop(exec {
                Log.d(TAG, "Drive loop")
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
    )
    //#endregion
    follower.startTeleOpDrive()

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

    bindSpawn(
        risingEdge { gamepad1.start },
        exec { follower.pose = Pose() }
    )
    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}