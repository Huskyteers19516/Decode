package org.firstinspires.ftc.teamcode.opmode

import com.bylazar.telemetry.PanelsTelemetry
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
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Flippers.Flipper
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.pedroPathing.Drawing
import org.firstinspires.ftc.teamcode.utils.Alliance

@Suppress("UNUSED")
val HuskyAuto = Mercurial.autonomous {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    val follower = Constants.createFollower(hardwareMap)

    var alliance = Alliance.RED
    val paths = Paths()
    paths.buildPaths(follower, alliance)

    schedule(
        deadline(
            wait {
                inLoop
            }, loop(exec {
                telemetry.addData("Status", "Initialized")
                telemetry.addLine("Press B for red, press X for blue")
                telemetry.addData("Current alliance", alliance)
                if (gamepad1.bWasPressed()) {
                    alliance = Alliance.RED
                    paths.buildPaths(follower, alliance)
                } else if (gamepad1.xWasPressed()) {
                    alliance = Alliance.BLUE
                    paths.buildPaths(follower, alliance)
                }
                telemetry.update()
            })
        )
    )

    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)
    val drive = Drive(hardwareMap)

    //#endregion

    fun followPath(path: PathChain) = sequence(exec {
        follower.followPath(path)
    }, wait { !follower.isBusy })

    fun shoot(flipper: Flipper) = sequence(
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

    fun shootAllThree() = sequence(
        shoot(Flipper.A),
        shoot(Flipper.B),
        shoot(Flipper.C)
    )

    fun doWithIntake(closure: Closure) = sequence(
        exec(intake::start),
        closure,
        exec(intake::stop)
    )

    waitForStart()
    schedule(
        sequence(
            deadline(
                wait(AutoConstants.CUTOFF_SECONDS),
                sequence(
                    exec { outtake.active = true },
                    followPath(paths.fromStartToShoot),
                    shootAllThree(),
                    doWithIntake(followPath(paths.pickUpFirstRow)),
                    followPath(paths.firstRowToShoot),
                    shootAllThree(),
                    doWithIntake(followPath(paths.pickUpSecondRow)),
                    followPath(paths.secondRowToShoot),
                    shootAllThree(),
                    doWithIntake(followPath(paths.pickUpThirdRow))
                ),
            ),
            exec {
                follower.holdPoint(paths.endLocation.withHeading(follower.heading))
            }
        )
    )

    schedule(
        loop(exec {
            intake.periodic(telemetryM)
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)


            drive.periodic(telemetryM)
            telemetryM.update(telemetry)
        })
    )
    dropToScheduler()
}