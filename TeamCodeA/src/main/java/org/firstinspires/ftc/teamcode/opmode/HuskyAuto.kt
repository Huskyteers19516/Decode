package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.hl
import com.pedropathing.paths.PathChain
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.repeat
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.AutoConstants.TIME_BETWEEN_SHOTS
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Feeders
import org.firstinspires.ftc.teamcode.hardware.Outtake

fun createHuskyAuto() = Mercurial.Program {
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

    val outtake = Outtake(hardwareMap)
    val feeders = Feeders(hardwareMap)

    //#endregion

    fun followPath(path: PathChain, maxPower: Double = 1.0, holdEnd: Boolean = true) = sequence(exec {
        drive.follower.followPath(path, maxPower, holdEnd)
    }, wait { !drive.follower.isBusy })


    fun shoot() = sequence(
        wait(outtake::canShoot),
        feeders.shoot(),
        wait(TIME_BETWEEN_SHOTS)
    )

    waitForStart()
    Log.d(TAG, paths.startPosition.toString())
    drive.follower.setStartingPose(paths.startPosition)
    Log.d(TAG, paths.aimHeading.toString())
    schedule(
        sequence(
            exec { outtake.active = true },
            followPath(paths.fromStartToShoot),
            repeat(3, shoot()),
            exec {
                drive.follower.holdPoint(paths.endLocation)
            }
        ),
    )

    schedule(
        loop(exec {
            outtake.periodic(telemetryM)
            drive.periodic(telemetryM)

            blackboard["x"] = drive.follower.pose.x
            blackboard["y"] = drive.follower.pose.y
            blackboard["heading"] = drive.follower.pose.heading

            telemetryM.update(telemetry)
        })
    )
    dropToScheduler()
}

@Suppress("UNUSED")
val HuskyAuto = Mercurial.autonomous("Husky Auto", "Huskyteers", "Husky TeleOp", createHuskyAuto())
