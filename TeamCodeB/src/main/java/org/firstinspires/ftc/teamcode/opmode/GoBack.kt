package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.geometry.BezierLine
import com.pedropathing.geometry.Pose
import com.pedropathing.paths.HeadingInterpolator
import com.pedropathing.paths.PathChain
import com.pedropathing.paths.PathPoint
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.match
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.parallel
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.scope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.Motif
import org.firstinspires.ftc.teamcode.utils.Slot
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("UNUSED")
val GoBack = Mercurial.autonomous {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    var alliance = Alliance.RED
    val drive = Drive(hardwareMap)
    val paths = Paths(drive.follower)

    schedule(
        deadline(
            wait {
                inLoop
            }, loop(exec {
                telemetryM.addData("Status", "Initialized")
                telemetryM.addLine("Press B for red, press X for blue")
                telemetryM.addData("Current alliance", alliance)
                if (gamepad1.bWasPressed()) {
                    alliance = Alliance.RED
                    paths.buildPaths(alliance)
                } else if (gamepad1.xWasPressed()) {
                    alliance = Alliance.BLUE
                    paths.buildPaths(alliance)
                }
                telemetryM.hl()


                telemetryM.update(telemetry)
            })
        )
    )


    //#endregion

    fun followPath(path: PathChain, maxPower: Double = 1.0, holdEnd: Boolean = true) = sequence(exec {
        drive.follower.followPath(path, maxPower, holdEnd)
    }, wait { !drive.follower.isBusy })



    waitForStart()



    val startPose = Pose(blackboard.getOrDefault("x", 0.0) as Double, blackboard.getOrDefault("y", 0.0) as Double, blackboard.getOrDefault("heading", 0.0) as Double)
    drive.follower.setStartingPose(startPose)
    drive.follower.followPath(drive.follower.pathBuilder().addPath(
        BezierLine(startPose, paths.startPosition)
    ).setLinearHeadingInterpolation(blackboard.getOrDefault("heading", 0.0) as Double, paths.startPosition.heading).build())

    schedule(
        loop(exec {
            drive.periodic(telemetryM)

            telemetryM.update(telemetry)
        })
    )
    dropToScheduler()
}