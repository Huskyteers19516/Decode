package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import org.firstinspires.ftc.teamcode.utils.hl

@Suppress("unused")
val LauncherElementTest = Mercurial.teleop("testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry

    fun getMotor(vararg names: String): DcMotorEx {
        for (name in names) {
            try {
                return hardwareMap.get(DcMotorEx::class.java, name)
            } catch (_: Exception) {
                // Try next configured name.
            }
        }
        throw IllegalArgumentException("Cannot find motor from names: ${names.joinToString()}")
    }

    val launcherMotor = getMotor("outtake", "launcher")
    val hoodMotor = getMotor("turretMotor", "turret", "hood")

    launcherMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    launcherMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    launcherMotor.setVelocityPIDFCoefficients(
        OuttakeConstants.SHOOTER_KP,
        OuttakeConstants.SHOOTER_KI,
        OuttakeConstants.SHOOTER_KD,
        OuttakeConstants.SHOOTER_KS
    )
    launcherMotor.power = 0.0

    hoodMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    hoodMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    hoodMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

    val launcherRpmStep = 50.0
    val hoodAngleStepDeg = 1.0
    val hoodTicksPerDegree = 180.0 / 360.0
    val launcherTicksPerRev = 28.0

    var targetLauncherRpm = 1200.0
    var targetHoodAngleDeg = 0.0

    bindSpawn(
        risingEdge { gamepad1.left_bumper },
        exec {
            targetLauncherRpm += launcherRpmStep
        }
    )

    bindSpawn(
        risingEdge { gamepad1.right_bumper },
        exec {
            targetLauncherRpm -= launcherRpmStep
        }
    )

    bindSpawn(
        risingEdge { gamepad1.a },
        exec {
            targetHoodAngleDeg += hoodAngleStepDeg
        }
    )

    bindSpawn(
        risingEdge { gamepad1.b },
        exec {
            targetHoodAngleDeg -= hoodAngleStepDeg
        }
    )

    waitForStart()

    schedule(
        loop(
            exec {
                targetLauncherRpm = Range.clip(targetLauncherRpm, 0.0, 6000.0)
                targetHoodAngleDeg = Range.clip(targetHoodAngleDeg, -180.0, 180.0)

                val launcherVelocityTicksPerSec = targetLauncherRpm * launcherTicksPerRev / 60.0
                launcherMotor.velocity = launcherVelocityTicksPerSec

                val hoodTargetTicks = (targetHoodAngleDeg * hoodTicksPerDegree).toInt()
                hoodMotor.targetPosition = hoodTargetTicks
                hoodMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
                hoodMotor.power = 0.4

                val currentLauncherRpm = launcherMotor.velocity / launcherTicksPerRev * 60.0
                val currentHoodAngleDeg = hoodMotor.currentPosition / hoodTicksPerDegree

                telemetryM.addLine("Launcher + Hood Test")
                telemetryM.addLine("left_bumper: RPM +, right_bumper: RPM -")
                telemetryM.addLine("A: hood angle +, B: hood angle -")
                telemetryM.hl()

                telemetryM.addData("Target launcher RPM", "%.1f".format(targetLauncherRpm))
                telemetryM.addData("Current launcher RPM", "%.1f".format(currentLauncherRpm))
                telemetryM.addData("Launcher velocity (ticks/s)", "%.1f".format(launcherMotor.velocity))
                telemetryM.hl()

                telemetryM.addData("Target hood angle (deg)", "%.1f".format(targetHoodAngleDeg))
                telemetryM.addData("Current hood angle (deg)", "%.1f".format(currentHoodAngleDeg))
                telemetryM.addData("Hood ticks", hoodMotor.currentPosition)
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}
