package org.firstinspires.ftc.teamcode.opmode

import com.bylazar.telemetry.PanelsTelemetry
import com.pedropathing.paths.HeadingInterpolator
import com.pedropathing.paths.PathChain
import dev.frozenmilk.dairy.mercurial.continuations.Closure
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.deadline
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.AutoConstants
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.Slot
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("UNUSED")
val TestingAuto = Mercurial.autonomous {

    /* ================= Pre-Init ================= */
    val telemetryM = PanelsTelemetry.telemetry

    var alliance = Alliance.RED
    val drive = Drive(hardwareMap)
    val paths = TestingPaths()
    val colorSensors = ColorSensors(hardwareMap)

    paths.buildPaths(drive.follower, alliance)

    schedule(
        deadline(
            wait { inLoop },
            loop(
                exec {
                    telemetryM.addData("Status", "Initialized")
                    telemetryM.addLine("Press B = RED, X = BLUE")
                    telemetryM.addData("Current Alliance", alliance)

                    if (gamepad1.bWasPressed()) {
                        alliance = Alliance.RED
                        paths.buildPaths(drive.follower, alliance)
                    } else if (gamepad1.xWasPressed()) {
                        alliance = Alliance.BLUE
                        paths.buildPaths(drive.follower, alliance)
                    }

                    telemetryM.hl()
                    colorSensors.update()
                    colorSensors.telemetry(telemetryM)
                    telemetryM.update(telemetry)
                }
            )
        )
    )

    val intake = Intake(hardwareMap)
    val outtake = Outtake(hardwareMap)
    val flippers = Flippers(hardwareMap)


    fun followPath(
        path: PathChain,
        maxPower: Double = 1.0,
        holdEnd: Boolean = true
    ) = sequence(
        exec { drive.follower.followPath(path, maxPower, holdEnd) },
        wait { !drive.follower.isBusy }
    )

    fun doWithIntake(closure: Closure) = sequence(
        exec(intake::start),
        closure,
        exec(intake::stop)
    )

    fun shoot(slot: Slot) = sequence(
        wait(outtake::canShoot),
        exec { flippers.raiseFlipper(slot) },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
        exec { flippers.lowerFlipper(slot) },
        wait(FlippersConstants.FLIPPER_WAIT_TIME),
    )

    fun shootAllThree() = sequence(
        shoot(Slot.A),
        shoot(Slot.B),
        shoot(Slot.C)
    )


    waitForStart()
    drive.follower.setStartingPose(paths.startPosition)

    schedule(
        sequence(
            deadline(
                wait(AutoConstants.CUTOFF_SECONDS),
                sequence(


                    exec { outtake.active = true },


                    followPath(
                        paths.fromStartToShoot.apply {
                            headingInterpolator =
                                HeadingInterpolator.facingPoint(Paths.obelisk)
                        }
                    ),


                    doWithIntake(
                        followPath(paths.pickUpSecondRow)
                    ),


                    followPath(paths.secondRowToShoot),


                    shootAllThree(),


                    loop(
                        sequence(


                            doWithIntake(
                                followPath(paths.shootToGate)
                            ),


                            followPath(paths.gateToShoot),


                            shootAllThree()
                        )
                    )
                )
            )
        )
    )


    schedule(
        loop(
            exec {
                intake.periodic(telemetryM)
                outtake.periodic(telemetryM)
                flippers.periodic(telemetryM)
                drive.periodic(telemetryM)
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}