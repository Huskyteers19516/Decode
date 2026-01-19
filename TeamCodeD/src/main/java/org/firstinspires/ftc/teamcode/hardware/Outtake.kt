package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import kotlin.math.abs

class Outtake(hardwareMap: HardwareMap) {
    private val outtakeMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "outtake")

    init {
        outtakeMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        outtakeMotor.setVelocityPIDFCoefficients(
            OuttakeConstants.KP,
            OuttakeConstants.KI,
            OuttakeConstants.KD,
            OuttakeConstants.KS
        )
        outtakeMotor.power = 0.0
    }

    var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    var active = false

    fun manualPeriodic(manualPower: Double, telemetry: TelemetryManager) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        outtakeMotor.power = manualPower
        telemetry.addData("Outtake active", active)
        telemetry.addData("Outtake power", outtakeMotor.power)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
    }

    fun periodic(telemetry: TelemetryManager, debugging: Boolean = false) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        outtakeMotor.setVelocityPIDFCoefficients(
            OuttakeConstants.KP,
            OuttakeConstants.KI,
            OuttakeConstants.KD,
            OuttakeConstants.KS
        )
        if (active) {
            outtakeMotor.velocity = targetVelocity
        } else {
            outtakeMotor.power = 0.0
        }
        telemetry.addData("Outtake active", active)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
        telemetry.addData("Outtake target velocity", targetVelocity)
        telemetry.addData("Outtake status", if (canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(): Boolean {
        return abs(targetVelocity - outtakeMotor.velocity) < OuttakeConstants.ALLOWANCE
    }

    fun toggle() {
        active = !active
    }
}