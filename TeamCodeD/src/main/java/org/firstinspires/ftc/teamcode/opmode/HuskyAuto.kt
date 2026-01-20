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
import org.firstinspires.ftc.teamcode.hardware.*
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.Slot
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("UNUSED")
val HuskyAuto = Mercurial.autonomous {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    var alliance = Alliance.RED
    val paths = Paths()
    val drive = Drive(hardwareMap)
    val colorSensors = ColorSensors(hardwareMap)
    paths.buildPaths(drive.follower, alliance)

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
                    paths.buildPaths(drive.follower, alliance)
                } else if (gamepad1.xWasPressed()) {
                    alliance = Alliance.BLUE
                    paths.buildPaths(drive.follower, alliance)
                }
                telemetryM.hl()
                colorSensors.update()
                colorSensors.telemetry(telemetryM)
                telemetryM.update(telemetry)
            })
        )
    )

    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)

    //#endregion

    fun followPath(path: PathChain) = sequence(exec {
        drive.follower.followPath(path, true)
    }, wait { !drive.follower.isBusy })

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

    fun shootAllThree() = sequence(
        shoot(Slot.A),
        shoot(Slot.B),
        shoot(Slot.C)
    )

    fun doWithIntake(closure: Closure) = sequence(
        exec(intake::start),
        closure,
        exec(intake::stop)
    )

    waitForStart()
    println(paths.startPosition)
    drive.follower.setStartingPose(paths.startPosition)
    schedule(
        sequence(
            deadline(
                wait(AutoConstants.CUTOFF_SECONDS),
                sequence(
                    exec { outtake.active = true },
                    followPath(paths.fromStartToShoot),
                    shootAllThree(),
                    doWithIntake(
                        followPath(paths.pickUpFirstRow)
                    ),
                    followPath(paths.firstRowToShoot),
                    shootAllThree(),
                    doWithIntake(followPath(paths.pickUpSecondRow)),
                    followPath(paths.secondRowToShoot),
                    shootAllThree(),
                    doWithIntake(followPath(paths.pickUpThirdRow))
                ),
            ),
            exec {
                drive.follower.holdPoint(paths.endLocation.withHeading(drive.follower.heading))
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