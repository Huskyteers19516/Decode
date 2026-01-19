package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.noop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutex
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutexes
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.utils.Slot


@Suppress("UNUSED")
val mre = Mercurial.teleop("MRE", "Huskyteers") {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    val outtake = Outtake(hardwareMap)
    val flippers = Flippers(hardwareMap)

    //#endregion

    waitForStart()

    // Drive controls

    // Variable solely for telemetry
    var isLaunching = false;

    val prioritiser = Mutexes.Prioritiser<Int> { new, old -> new >= old }

    val flipperMutex = Mutex(prioritiser, Unit)

    // Our robot has 3 independent flippers, the concept can be seen here: https://youtu.be/eq2MiUJNWEM?t=8
    // When the button that corresponds to a flipper is pressed, the outtake starts to spin up (priority 0 mutex acquisition)
    // If another button is pressed during this spin up time, it replaces the other fiber (since the prioritizer is >=)
    // Once it gets up to speed, it acquires the mutex with priority 1, which cannot be interrupted by any other flipper launch
    // This way, none of the flippers collide

    fun generateFlipperSequence(flipper: Slot) =
        Mutexes.guardPoll(
            flipperMutex,
            { 0 },
            { _ ->
                sequence(
                    exec { outtake.active = true },
                    wait { outtake.canShoot() },
                    Mutexes.guardPoll(
                        flipperMutex,
                        { 1 },
                        { _ ->
                            sequence(
                                exec {
                                    isLaunching = true
                                    flippers.raiseFlipper(flipper)
                                },
                                wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                exec { flippers.lowerFlipper(flipper) },

                                wait(FlippersConstants.FLIPPER_WAIT_TIME),
                                exec { isLaunching = false }
                            )
                        },
                        // should be impossible
                        noop(),
                        noop()
                    ),
                )
            },
            noop(),
            noop()
        )

    bindSpawn(
        risingEdge {
            gamepad1.back
        },
        exec {
            flipperMutex // put breakpoint here to capture
        }
    )


    val fiberA = bindSpawn(
        risingEdge { gamepad1.a },
        generateFlipperSequence(Slot.A)
    )

    val fiberB = bindSpawn(
        risingEdge { gamepad1.b },
        generateFlipperSequence(Slot.B)
    )

    val fiberC = bindSpawn(
        risingEdge { gamepad1.x },
        generateFlipperSequence(Slot.C)
    )

    bindSpawn(
        risingEdge {
            gamepad2.a
        }, Mutexes.guardPoll(
            flipperMutex,
            { -1 },
            { _ ->
                exec { outtake.toggle() }
            },
            noop(),
            noop()
        )
    )


    // Main loop
    schedule(
        loop(exec {
            outtake.periodic(telemetryM)
            flippers.periodic(telemetryM)

            telemetry.addData("Is Launching", isLaunching)
            telemetry.addData("Fiber A", fiberA.state)
            telemetry.addData("Fiber B", fiberB.state)
            telemetry.addData("Fiber C", fiberC.state)
            telemetryM.update(telemetry)
        })
    )

    Log.d(TAG, "HuskyTeleOp started")
    dropToScheduler()
}