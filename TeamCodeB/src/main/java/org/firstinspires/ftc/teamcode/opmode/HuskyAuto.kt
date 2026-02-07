package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.paths.HeadingInterpolator
import com.pedropathing.paths.PathChain
import com.pedropathing.paths.PathPoint
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.jumpScope
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.match
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.opmode.pathpackage.AutoNumber1
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.Motif
import com.huskyteers19516.shared.Slot
import com.huskyteers19516.shared.hl
import kotlin.math.abs

fun createHuskyAuto() = Mercurial.Program {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;


    // Changing Auto route number when it is necessarily. Comment out old one and put new one in
    var alliance = Alliance.RED
    val drive = Drive(hardwareMap)
    //val paths = Paths(drive.follower)
    //val paths = Autonumber2(drive.follower)
    //val paths =
    val paths = AutoNumber1(drive.follower)
    val camera = Camera(hardwareMap)
    val colorSensors = ColorSensors(hardwareMap)

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
                colorSensors.update()
                colorSensors.telemetry(telemetryM)
                if (colorSensors.slots[Slot.A] == ColorSensors.Companion.Artifact.GREEN) {
                    telemetryM.addLine("WARNING: Green in slot A (back slot). Recommended to put purple artifact there.")
                }
                if (colorSensors.slots.count {
                        it.value in listOf(
                            ColorSensors.Companion.Artifact.GREEN,
                            ColorSensors.Companion.Artifact.PURPLE
                        )
                    } < 3) {
                    telemetryM.addLine("WARNING: Currently detecting less than 3 artifacts")
                } else if (colorSensors.slots.count { it.value in listOf(ColorSensors.Companion.Artifact.GREEN) } != 1) {
                    telemetryM.addLine("WARNING: Detecting something other than 1 green artifact and 2 purple artifacts.")
                }

                telemetryM.update(telemetry)
            })
        )
    )

    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)

    var motif: Motif? = null

    //#endregion

    fun followPath(path: PathChain, maxPower: Double = 1.0, holdEnd: Boolean = true) = sequence(exec {
        drive.follower.followPath(path, maxPower, holdEnd)
    }, wait { !drive.follower.isBusy })

    fun turnTo(radians: Double) = sequence(
        deadline(
            wait(1.5), sequence(
                exec {
                    drive.follower.turnTo(radians)
                },
                wait { abs(drive.follower.pose.heading - radians) < 0.007 },
                jumpScope {
                    loop(exec {
                        // once it sees the april tag, stops aligning
                        camera.getTargetTag(alliance)
                            ?.let { drive.orientTowardsAprilTag(it, false); Log.d(TAG, "found april tag"); jump() }
                    })
                },
            )
        )
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
        match { Log.d(TAG, "matching motif"); motif }
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
                camera.getTargetTag(alliance)
                    ?.let { drive.orientTowardsAprilTag(it, false); Log.d(TAG, "found april tag"); jump() }
            })
        },

        )

    // todo: empty extras function

    fun shootRemaining() = deadline(
        wait { colorSensors.slots.all { it.value == ColorSensors.Companion.Artifact.NONE } }, loop(
            shootColor(ColorSensors.Companion.Artifact.GREEN)
        )
    )

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
        sequence(
            deadline(
                wait(AutoConstants.CUTOFF_SECONDS),
                sequence(
                    exec { outtake.active = true },
                    followPath(paths.fromStartToShoot.apply {
                        headingInterpolator = object : HeadingInterpolator {
                            val faceObelisk = HeadingInterpolator.facingPoint(Paths.obelisk)
                            val faceGoal = HeadingInterpolator.tangent.reverse()
                            override fun interpolate(closestPoint: PathPoint?): Double {
                                return if (motif == null) {
                                    faceObelisk.interpolate(closestPoint)
                                } else {
                                    faceGoal.interpolate(closestPoint)
                                }
                            }
                        }
                    }),
                    turnTo(paths.aimHeading),
                    exec {
                        if (motif == null) {
                            motif = Motif.PGP
                        }
                        Log.d(TAG, "Got motif")
                    },
                    shootAllThree(),
                    shootRemaining(),
                    exec {
                        needColorSensors = true
                    },
                    doWithIntake(
                        sequence(followPath(paths.pickUpFirstRow), followPath(paths.firstRowToShoot))
                    ),
                    turnTo(paths.aimHeading),
                    shootAllThree(),
                    shootRemaining(),
                    doWithIntake(sequence(followPath(paths.pickUpSecondRow), followPath(paths.secondRowToShoot))),
                    turnTo(paths.aimHeading),
                    shootAllThree(),
                    shootRemaining(),
                    doWithIntake(followPath(paths.pickUpThirdRow))
                ),
            ),
            exec {
                drive.follower.breakFollowing()
                drive.follower.holdPoint(paths.endLocation.withHeading(drive.follower.heading))
            }
        )
    )

    schedule(
        loop(exec {
            telemetry.addData("Motif", motif)
            intake.periodic(telemetryM)
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)
            drive.periodic(telemetryM)
            if (needColorSensors) {
                colorSensors.update()
            }
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

val HuskyAuto = Mercurial.autonomous("Husky Auto", "Huskyteers", "Husky TeleOp", createHuskyAuto())
