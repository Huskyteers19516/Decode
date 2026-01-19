package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutex
import dev.frozenmilk.dairy.mercurial.continuations.mutexes.Mutexes
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import java.lang.Thread.sleep


@Suppress("UNUSED")
val mutexTester = Mercurial.teleop("Mutex Test", "Huskyteers") {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    //#endregion

    waitForStart()

    // Drive controls


    var isLaunching = "Stopped"

    val prioritiser = Mutexes.Prioritiser<Int> { new, old -> new >= old }

    val flipperMutex = Mutex(prioritiser, Unit)

    bindSpawn(
        risingEdge {
            gamepad1.back
        },
        exec {
            flipperMutex // put breakpoint here to capture flipperMutex
        }
    )

    val a = bindSpawn(
        risingEdge {
            gamepad1.a
        },
        sequence(
            exec { Log.d(TAG, "A Pressed") }, Mutexes.guardPoll(
                flipperMutex,
                { 0 },
                { _ ->
                    sequence(
                        exec {
                            Log.d(TAG, "launch a")
                            isLaunching = "A"
                        },
                        wait(3.0),
                        exec {
                            Log.d(TAG, "stop a")
                            isLaunching = "A Stopped"
                        }
                    )
                },
                exec {
                    Log.d(TAG, "A rejected")
                },
                exec {
                    Log.d(TAG, "A canceled")
                }
            ))
    )

    val b = bindSpawn(
        risingEdge {
            gamepad1.b
        },
        sequence(
            exec { Log.d(TAG, "B Pressed") }, Mutexes.guardPoll(
                flipperMutex,
                { 0 },
                { _ ->
                    sequence(
                        exec {
                            Log.d(TAG, "launch B")
                            isLaunching = "B"
                        },
                        wait(3.0),
                        exec {
                            Log.d(TAG, "stop b")
                            isLaunching = "B Stopped"
                        }
                    )
                },
                exec {
                    Log.d(TAG, "B rejected")
                },
                exec {
                    Log.d(TAG, "B canceled")
                }
            )
        ))

    schedule(
        loop(exec {
            telemetryM.addData("A", a.state)
            telemetryM.addData("B", b.state)
            telemetryM.addLine("Launching: $isLaunching")
            telemetryM.update(telemetry)
            // added sleep because it seems that without it, the loop is so fast that the debounce is not working correctly
            sleep(1)
        })
    )

    dropToScheduler()
}