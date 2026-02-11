package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import com.huskyteers19516.shared.hl
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants

@Suppress("unused")
val LauncherElementTest = Mercurial.teleop("testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry

    fun getMotor(vararg names: String): DcMotorEx {
        for (name in names) {
            try {
                return hardwareMap.get(DcMotorEx::class.java, name)
            } catch (_: Exception) {

            }
        }
        throw IllegalArgumentException("Cannot find motor from names: ${names.joinToString()}")
    }

    fun getServo(vararg names: String): Servo {
        for (name in names) {
            try {
                return hardwareMap.get(Servo::class.java, name)
            } catch (_: Exception) {
            }
        }
        throw IllegalArgumentException("Cannot find servo from names: ${names.joinToString()}")
    }

    val launcherMotor = getMotor("outtake", "launcher")
    val hoodServo = getServo("hood", "launcherHood")

    launcherMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    launcherMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
    launcherMotor.setVelocityPIDFCoefficients(
        OuttakeConstants.SHOOTER_KP,
        OuttakeConstants.SHOOTER_KI,
        OuttakeConstants.SHOOTER_KD,
        OuttakeConstants.SHOOTER_KS
    )
    launcherMotor.power = 0.0

    val launcherRpmStep = 50.0
    val hoodAngleStepDeg = 1.0
    val hoodServoCalStep = 0.005
    val hoodMinAngleDeg = 0.0
    val hoodMaxAngleDeg = 60.0
    var hoodMinServoPosCal = 0.10
    var hoodMaxServoPosCal = 0.80
    val hoodServoReversed = false
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

    // Hood servo calibration:
    // X/Y adjust min position, dpad left/right adjust max position.
    bindSpawn(
        risingEdge { gamepad1.x },
        exec {
            hoodMinServoPosCal -= hoodServoCalStep
        }
    )

    bindSpawn(
        risingEdge { gamepad1.y },
        exec {
            hoodMinServoPosCal += hoodServoCalStep
        }
    )

    bindSpawn(
        risingEdge { gamepad1.dpad_left },
        exec {
            hoodMaxServoPosCal -= hoodServoCalStep
        }
    )

    bindSpawn(
        risingEdge { gamepad1.dpad_right },
        exec {
            hoodMaxServoPosCal += hoodServoCalStep
        }
    )

    waitForStart()

    schedule(
        loop(
            exec {
                targetLauncherRpm = Range.clip(targetLauncherRpm, 0.0, 2000.0)
                targetHoodAngleDeg = Range.clip(targetHoodAngleDeg, hoodMinAngleDeg, hoodMaxAngleDeg)
                hoodMinServoPosCal = Range.clip(hoodMinServoPosCal, 0.0, 1.0)
                hoodMaxServoPosCal = Range.clip(hoodMaxServoPosCal, 0.0, 1.0)

                // Keep a safe separation between min/max calibration points.
                if (hoodMaxServoPosCal - hoodMinServoPosCal < 0.01) {
                    hoodMaxServoPosCal = Range.clip(hoodMinServoPosCal + 0.01, 0.0, 1.0)
                }

                val launcherVelocityTicksPerSec = targetLauncherRpm * launcherTicksPerRev / 60.0
                launcherMotor.velocity = launcherVelocityTicksPerSec

                val hoodRatio =
                    (targetHoodAngleDeg - hoodMinAngleDeg) / (hoodMaxAngleDeg - hoodMinAngleDeg)
                val mappedRatio = if (hoodServoReversed) 1.0 - hoodRatio else hoodRatio
                val hoodServoTargetPos =
                    hoodMinServoPosCal + mappedRatio * (hoodMaxServoPosCal - hoodMinServoPosCal)
                hoodServo.position = Range.clip(hoodServoTargetPos, 0.0, 1.0)

                val currentLauncherRpm = launcherMotor.velocity / launcherTicksPerRev * 60.0
                val currentHoodAngleDeg = hoodMinAngleDeg +
                    ((if (hoodServoReversed) hoodMaxServoPosCal - hoodServo.position
                    else hoodServo.position - hoodMinServoPosCal) *
                    (hoodMaxAngleDeg - hoodMinAngleDeg) / (hoodMaxServoPosCal - hoodMinServoPosCal))

                telemetryM.addLine("Launcher + Hood Test")
                telemetryM.addLine("left_bumper: RPM +, right_bumper: RPM -")
                telemetryM.addLine("A: hood angle +, B: hood angle -")
                telemetryM.addLine("X/Y: hood min -, +  | dpad left/right: hood max -, +")
                telemetryM.hl()

                telemetryM.addData("Target launcher RPM", "%.1f".format(targetLauncherRpm))
                telemetryM.addData("Current launcher RPM", "%.1f".format(currentLauncherRpm))
                telemetryM.addData("Launcher velocity (ticks/s)", "%.1f".format(launcherMotor.velocity))
                telemetryM.hl()

                telemetryM.addData("Target hood angle (deg)", "%.1f".format(targetHoodAngleDeg))
                telemetryM.addData("Current hood angle (deg)", "%.1f".format(currentHoodAngleDeg))
                telemetryM.addData("Hood servo pos", "%.3f".format(hoodServo.position))
                telemetryM.addData("Hood min servo cal", "%.3f".format(hoodMinServoPosCal))
                telemetryM.addData("Hood max servo cal", "%.3f".format(hoodMaxServoPosCal))
                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}
