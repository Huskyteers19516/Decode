package org.firstinspires.ftc.teamcode.opmode.testing

import android.util.Log
import com.bylazar.telemetry.PanelsTelemetry
import com.huskyteers19516.shared.Alliance
import com.huskyteers19516.shared.Slot
import com.huskyteers19516.shared.hl
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.loop
import dev.frozenmilk.dairy.mercurial.ftc.Mercurial
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import org.firstinspires.ftc.teamcode.constants.TeleOpConstants
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.hardware.ColorSensors
import org.firstinspires.ftc.teamcode.hardware.Drive
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake

private enum class TestMode {
    DRIVE,
    SHOOTER,
    SYSTEM,
    TURRET,
    COLOR_SENSOR,
    CAMERA,
    LAUNCHER
}

@Suppress("bigMac")
val BigMacBundletesting= Mercurial.teleop("All In One Testing", "Testing") {
    val telemetryM = PanelsTelemetry.telemetry

    fun nextMode(mode: TestMode): TestMode {
        val list = TestMode.entries
        return list[(mode.ordinal + 1) % list.size]
    }

    fun prevMode(mode: TestMode): TestMode {
        val list = TestMode.entries
        return list[(mode.ordinal - 1 + list.size) % list.size]
    }

    fun getMotorOrNull(vararg names: String): DcMotorEx? {
        for (name in names) {
            try {
                return hardwareMap.get(DcMotorEx::class.java, name)
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun getServoOrNull(vararg names: String): Servo? {
        for (name in names) {
            try {
                return hardwareMap.get(Servo::class.java, name)
            } catch (_: Exception) {
            }
        }
        return null
    }

    val drive = Drive(hardwareMap)
    val outtake = Outtake(hardwareMap)
    val intake = Intake(hardwareMap)
    val flippers = Flippers(hardwareMap)
    val camera = Camera(hardwareMap)
    val colorSensors = ColorSensors(hardwareMap)

    val turretMotor = getMotorOrNull("turretMotor")
    turretMotor?.let {
        it.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        it.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    val launcherMotor = getMotorOrNull("outtake", "launcher")
    val hoodServo = getServoOrNull("hood", "launcherHood")

    launcherMotor?.let {
        it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        it.mode = DcMotor.RunMode.RUN_USING_ENCODER
        it.setVelocityPIDFCoefficients(
            OuttakeConstants.SHOOTER_KP,
            OuttakeConstants.SHOOTER_KI,
            OuttakeConstants.SHOOTER_KD,
            OuttakeConstants.SHOOTER_KS
        )
        it.power = 0.0
    }

    var mode = TestMode.DRIVE
    var modeLocked = false
    var driveIndividualMode = true
    var shooterVelocityMode = true
    var systemVelocityMode = true

    var turretTargetTicks = 0
    var turretAutoMove = false

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

    fun boolToPower(a: Boolean) = if (a) 1.0 else 0.0

    schedule(
        camera.waitForCamera(telemetryM) {
            telemetryM.update(telemetry)
        }
    )

    waitForStart()
    drive.follower.startTeleopDrive()

    bindExec(risingEdge { !modeLocked && gamepad1.dpad_up }, exec { mode = prevMode(mode) })
    bindExec(risingEdge { !modeLocked && gamepad1.dpad_down }, exec { mode = nextMode(mode) })
    bindExec(risingEdge { !modeLocked && gamepad1.a }, exec { modeLocked = true })
    bindExec(risingEdge { modeLocked && gamepad1.back }, exec { modeLocked = false })

    bindExec(risingEdge { modeLocked && gamepad1.start }, exec {
        when (mode) {
            TestMode.DRIVE -> driveIndividualMode = !driveIndividualMode
            TestMode.SHOOTER -> shooterVelocityMode = !shooterVelocityMode
            TestMode.SYSTEM -> systemVelocityMode = !systemVelocityMode
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.dpad_up }, exec {
        when (mode) {
            TestMode.DRIVE -> drive.resetOrientation()
            TestMode.SHOOTER, TestMode.SYSTEM -> {
                outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
            }
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.dpad_down }, exec {
        when (mode) {
            TestMode.DRIVE -> drive.isRobotCentric = !drive.isRobotCentric
            TestMode.SHOOTER, TestMode.SYSTEM -> {
                outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR
            }
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.dpad_left }, exec {
        when (mode) {
            TestMode.SHOOTER, TestMode.SYSTEM -> {
                outtake.targetVelocity -= TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
            }
            TestMode.LAUNCHER -> hoodMaxServoPosCal -= hoodServoCalStep
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.dpad_right }, exec {
        when (mode) {
            TestMode.SHOOTER, TestMode.SYSTEM -> {
                outtake.targetVelocity += TeleOpConstants.OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR
            }
            TestMode.LAUNCHER -> hoodMaxServoPosCal += hoodServoCalStep
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.y }, exec {
        when (mode) {
            TestMode.SHOOTER, TestMode.SYSTEM -> outtake.toggle()
            TestMode.TURRET -> {
                turretMotor?.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                turretMotor?.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                turretTargetTicks = 0
            }
            TestMode.LAUNCHER -> hoodMinServoPosCal += hoodServoCalStep
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && gamepad1.a }, exec {
        when (mode) {
            TestMode.SYSTEM -> flippers.raiseFlipper(Slot.A)
            TestMode.TURRET -> {
                turretMotor?.targetPosition = turretTargetTicks + 500
                turretTargetTicks += 500
                turretMotor?.mode = DcMotor.RunMode.RUN_TO_POSITION
                turretMotor?.power = 0.5
                turretAutoMove = true
            }
            TestMode.CAMERA -> {
                camera.getTargetTag(Alliance.BLUE)?.let {
                    drive.orientTowardsAprilTag(it, false)
                    Log.d("TESTING", "found april tag")
                }
            }
            TestMode.LAUNCHER -> targetHoodAngleDeg += hoodAngleStepDeg
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && !gamepad1.a && mode == TestMode.SYSTEM }, exec {
        flippers.lowerFlipper(Slot.A)
    })

    bindExec(risingEdge { modeLocked && gamepad1.b }, exec {
        when (mode) {
            TestMode.SYSTEM -> flippers.raiseFlipper(Slot.B)
            TestMode.TURRET -> {
                turretMotor?.targetPosition = turretTargetTicks - 500
                turretTargetTicks -= 500
                turretMotor?.mode = DcMotor.RunMode.RUN_TO_POSITION
                turretMotor?.power = 0.5
                turretAutoMove = true
            }
            TestMode.LAUNCHER -> targetHoodAngleDeg -= hoodAngleStepDeg
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && !gamepad1.b && mode == TestMode.SYSTEM }, exec {
        flippers.lowerFlipper(Slot.B)
    })

    bindExec(risingEdge { modeLocked && gamepad1.x }, exec {
        when (mode) {
            TestMode.SYSTEM -> flippers.raiseFlipper(Slot.C)
            TestMode.DRIVE -> driveIndividualMode = !driveIndividualMode
            TestMode.LAUNCHER -> hoodMinServoPosCal -= hoodServoCalStep
            else -> {}
        }
    })

    bindExec(risingEdge { modeLocked && !gamepad1.x && mode == TestMode.SYSTEM }, exec {
        flippers.lowerFlipper(Slot.C)
    })

    bindExec(risingEdge { modeLocked && gamepad1.left_bumper && mode == TestMode.LAUNCHER }, exec {
        targetLauncherRpm += launcherRpmStep
    })

    bindExec(risingEdge { modeLocked && gamepad1.right_bumper && mode == TestMode.LAUNCHER }, exec {
        targetLauncherRpm -= launcherRpmStep
    })

    schedule(
        loop(
            exec {
                if (!modeLocked) {
                    telemetryM.addLine("Big Mac Bundle Testing")
                    telemetryM.addLine("Select test first")
                    telemetryM.addLine("dpad_up/down: choose test")
                    telemetryM.addLine("A: confirm and run")
                    telemetryM.addData("Selected test", mode.name)
                    telemetryM.update(telemetry)
                    return@exec
                }

                telemetryM.addLine("Big Mac Bundle Testing")
                telemetryM.addData("Running test", mode.name)
                telemetryM.addLine("Press BACK to return to test selection")
                telemetryM.hl()

                when (mode) {
                    TestMode.DRIVE -> {
                        telemetryM.addLine("Drive: start switch mode, dpad_up reset heading, dpad_down toggle field/robot")
                        if (driveIndividualMode) {
                            telemetryM.addLine("Individual: X=FL, Y=FR, A=RL, B=RR")
                            drive.debugPeriodic(
                                boolToPower(gamepad1.x),
                                boolToPower(gamepad1.y),
                                boolToPower(gamepad1.a),
                                boolToPower(gamepad1.b)
                            )
                        } else {
                            drive.manualPeriodic(
                                -gamepad1.left_stick_y.toDouble(),
                                -gamepad1.left_stick_x.toDouble(),
                                -gamepad1.right_stick_x.toDouble(),
                                telemetryM,
                            )
                        }
                        drive.debugTelemetry(telemetryM)
                    }

                    TestMode.SHOOTER -> {
                        telemetryM.addLine("Shooter: start toggle velocity/manual, dpad adjust, Y toggle")
                        if (shooterVelocityMode) outtake.periodic(telemetryM, true)
                        else outtake.manualPeriodic(-gamepad1.right_stick_y.toDouble(), telemetryM)
                    }

                    TestMode.SYSTEM -> {
                        telemetryM.addLine("System: left stick(intake), A/B/X flippers, dpad+Y outtake, start mode")
                        if (systemVelocityMode) outtake.periodic(telemetryM, true)
                        else outtake.manualPeriodic(-gamepad1.right_stick_y.toDouble(), telemetryM)
                        intake.manualPeriodic(-gamepad2.left_stick_y.toDouble(), telemetryM)
                        flippers.periodic(telemetryM, true)
                        camera.debugTelemetry(telemetryM)
                    }

                    TestMode.TURRET -> {
                        if (turretMotor == null) {
                            telemetryM.addLine("No turret motor found (name: turretMotor)")
                        } else {
                            val manualPower = -gamepad1.right_stick_x.toDouble()
                            if (kotlin.math.abs(manualPower) > 0.1) {
                                turretAutoMove = false
                                turretMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                                turretMotor.power = manualPower * 0.6
                            } else if (!turretAutoMove) {
                                turretMotor.power = 0.0
                            }

                            val guessedTicksPerDegree = 537.7 / 360.0
                            val estimatedDegrees = turretMotor.currentPosition / guessedTicksPerDegree

                            telemetryM.addLine("Turret: right stick X manual, A/B +/-500 ticks, Y reset")
                            telemetryM.addData("Current Ticks", turretMotor.currentPosition)
                            telemetryM.addData("Estimated Degrees", "%.2f".format(estimatedDegrees))
                            telemetryM.addData("Motor Mode", turretMotor.mode)
                        }
                    }

                    TestMode.COLOR_SENSOR -> {
                        telemetryM.addLine("Color Sensor test")
                        colorSensors.debugTelemetry(telemetryM)
                    }

                    TestMode.CAMERA -> {
                        telemetryM.addLine("Camera: press A to orient to BLUE target tag")
                        drive.periodic(telemetryM)
                        try {
                            camera.debugTelemetry(telemetryM)
                        } catch (e: Exception) {
                            telemetryM.addLine("Camera error")
                        }
                    }

                    TestMode.LAUNCHER -> {
                        if (launcherMotor == null || hoodServo == null) {
                            telemetryM.addLine("Launcher hardware missing (outtake/launcher + hood/launcherHood)")
                        } else {
                            targetLauncherRpm = Range.clip(targetLauncherRpm, 0.0, 2000.0)
                            targetHoodAngleDeg = Range.clip(targetHoodAngleDeg, hoodMinAngleDeg, hoodMaxAngleDeg)
                            hoodMinServoPosCal = Range.clip(hoodMinServoPosCal, 0.0, 1.0)
                            hoodMaxServoPosCal = Range.clip(hoodMaxServoPosCal, 0.0, 1.0)
                            if (hoodMaxServoPosCal - hoodMinServoPosCal < 0.01) {
                                hoodMaxServoPosCal = Range.clip(hoodMinServoPosCal + 0.01, 0.0, 1.0)
                            }

                            launcherMotor.velocity = targetLauncherRpm * launcherTicksPerRev / 60.0
                            val hoodRatio =
                                (targetHoodAngleDeg - hoodMinAngleDeg) / (hoodMaxAngleDeg - hoodMinAngleDeg)
                            val mappedRatio = if (hoodServoReversed) 1.0 - hoodRatio else hoodRatio
                            hoodServo.position = Range.clip(
                                hoodMinServoPosCal + mappedRatio * (hoodMaxServoPosCal - hoodMinServoPosCal),
                                0.0,
                                1.0
                            )

                            val currentLauncherRpm = launcherMotor.velocity / launcherTicksPerRev * 60.0
                            telemetryM.addLine("Launcher: LB/RB RPM +/- | A/B hood angle +/- | X/Y min cal | dpad L/R max cal")
                            telemetryM.addData("Target launcher RPM", "%.1f".format(targetLauncherRpm))
                            telemetryM.addData("Current launcher RPM", "%.1f".format(currentLauncherRpm))
                            telemetryM.addData("Hood servo pos", "%.3f".format(hoodServo.position))
                            telemetryM.addData("Hood min servo cal", "%.3f".format(hoodMinServoPosCal))
                            telemetryM.addData("Hood max servo cal", "%.3f".format(hoodMaxServoPosCal))
                        }
                    }
                }

                telemetryM.update(telemetry)
            }
        )
    )

    dropToScheduler()
}
