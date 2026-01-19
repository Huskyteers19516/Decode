package org.firstinspires.ftc.teamcode.opmode

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial


@Suppress("UNUSED")
val buttonTester = Mercurial.teleop("Button test") {
    //#region Pre-Init
    val telemetryM = PanelsTelemetry.telemetry;

    //#endregion

    waitForStart()

    // Drive controls

    val a = bindSpawn(
        risingEdge {
            gamepad1.a
        },
        exec { Log.d(TAG, "A Pressed") }
    )

    dropToScheduler()
}