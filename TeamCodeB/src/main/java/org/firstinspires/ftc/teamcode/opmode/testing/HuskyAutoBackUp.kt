package org.firstinspires.ftc.teamcode.opmode.testing

import org.firstinspires.ftc.teamcode.opmode.Paths
import org.firstinspires.ftc.teamcode.opmode.TAG



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
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.opmode.pathpackage.AutoNumber3
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.Motif
import com.huskyteers19516.shared.hl
import kotlin.math.abs

fun createHuskyAuto() = Mercurial.Program {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;


    // Changing Auto route number when it is necessarily. Comment out old one and put new one in
    var alliance = Alliance.RED
    val drive = Drive(hardwareMap)
    val paths = AutoNumber3(drive.follower)
    val camera = Camera(hardwareMap)

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
    val intake = Intake(hardwareMap)
    val transcript = hardwareMap.get(DcMotorEx::class.java, "transcript").apply {
        mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        power = 0.0
    }

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

    fun shoot() = sequence(
        wait(outtake::canShoot),
        exec {
            transcript.power = 1.0
        },
        wait(0.25),
        exec {
            transcript.power = 0.0
        },
        wait(0.1),
    )

    fun shootAllThree() = deadline(
        match { Log.d(TAG, "matching motif"); motif }
            .branch(
                Motif.GPP,
                sequence(
                    shoot(),
                    shoot(),
                    shoot(),
                )
            )
            .branch(
                Motif.PGP,
                sequence(
                    shoot(),
                    shoot(),
                    shoot(),
                )
            )
            .branch(
                Motif.PPG,
                sequence(
                    shoot(),
                    shoot(),
                    shoot(),
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

    fun shootRemaining() = noop()

    fun doWithIntake(closure: Closure) = sequence(
        exec(intake::start),
        closure,
        exec(intake::stop),
        exec {
            drive.follower.setMaxPower(1.0);
        }
    )

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
                    doWithIntake(
                        sequence(followPath(paths.pickUpSecondRow), followPath(paths.secondRowToShoot))
                    ),
                    turnTo(paths.aimHeading),
                    shootAllThree(),
                    shootRemaining(),
                    doWithIntake(sequence(followPath(paths.pickUpGoal), followPath(paths.goalRowToShoot))),
                    turnTo(paths.aimHeading),
                    shootAllThree(),
                    shootRemaining(),
                    doWithIntake(sequence(followPath(paths.pickUpGoal), followPath(paths.goalRowToShoot))),
                    turnTo(paths.aimHeading),
                    shootAllThree(),
                    shootRemaining(),
                    doWithIntake(sequence(followPath(paths.pickUpGoal))),
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
            drive.periodic(telemetryM)
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
