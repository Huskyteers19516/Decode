package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
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
import kotlin.math.abs

@Suppress("UNUSED")
val TestAuto = Mercurial.autonomous {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    var alliance = Alliance.RED
    val drive = Drive(hardwareMap)
    val paths = Paths(drive.follower)
    val camera = Camera(hardwareMap)
    val colorSensors = ColorSensors(hardwareMap)


    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)

    var motif: Motif? = null

    //#endregion

    fun followPath(path: PathChain, maxPower: Double = 1.0, holdEnd: Boolean = true) = sequence(exec {
        drive.follower.followPath(path, maxPower, holdEnd)
    }, wait { !drive.follower.isBusy })

    fun turnTo(radians: Double) = sequence(deadline(wait(1.5), sequence(
        exec {
            drive.follower.turnTo(radians)
        }, wait { abs(drive.follower.pose.heading - radians) < 0.007 }))
    )

    fun shoot(flipper: Slot) = sequence(
        wait(outtake::canShoot),
        exec {
            flippers.raiseFlipper(flipper)
        },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
        exec {
            flippers.lowerFlipper(flipper)
        },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
    )


    fun shootColor(color: ColorSensors.Companion.Artifact) = match { colorSensors.getBestSlot(color) }
        .branch(
            Slot.A, shoot(Slot.A)
        )
        .branch(Slot.B, shoot(Slot.B))
        .branch(Slot.C, shoot(Slot.C))
        .defaultBranch(noop())

    fun shootAllThree() = deadline(
        match { Log.d(TAG, "matching motif");motif }
            .branch(
                Motif.GPP,
                sequence(
                    shootColor(ColorSensors.Companion.Artifact.GREEN),
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                )
            )
            .branch(
                Motif.PGP,
                sequence(
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                    shootColor(ColorSensors.Companion.Artifact.GREEN),
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                )
            )
            .branch(
                Motif.PPG,
                sequence(
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                    shootColor(ColorSensors.Companion.Artifact.PURPLE),
                    shootColor(ColorSensors.Companion.Artifact.GREEN),
                )
            ),
        jumpScope {
            loop(exec {
                // once it sees the april tag, stops aligning
                camera.getTargetTag(alliance)?.let { drive.orientTowardsAprilTag(it); Log.d(TAG, "found april tag"); jump() }
            })
        },

    )

    // todo: empty extras function

    fun shootRemaining() = deadline(
        wait { colorSensors.slots.all { it.value == ColorSensors.Companion.Artifact.NONE } }, loop(
        shootColor(ColorSensors.Companion.Artifact.GREEN)
    ))

    fun doWithIntake(closure: Closure) = sequence(
        exec(intake::start),
        closure,
        exec(intake::stop),
        exec {
            drive.follower.setMaxPower(1.0);
        }
    )

    var needColorSensors = false

    waitForStart()
    Log.d(TAG, paths.startPosition.toString())
    drive.follower.setStartingPose(paths.startPosition)
    Log.d(TAG, paths.aimHeading.toString())
    schedule(
        turnTo(1.0)
    )

    schedule(
        loop(exec {
            telemetry.addData("Motif", motif)
            intake.periodic(telemetryM)
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)
            drive.periodic(telemetryM)
            if (needColorSensors) { colorSensors.update() }
            colorSensors.telemetry(telemetryM)
            if (motif == null) {
                motif = camera.getObelisk()
            }

            blackboard["x"] = drive.follower.pose.x
            blackboard["y"] = drive.follower.pose.y
            blackboard["heading"] = drive.follower.pose.heading


            telemetryM.update(telemetry)
        })
    )
    dropToScheduler()
}