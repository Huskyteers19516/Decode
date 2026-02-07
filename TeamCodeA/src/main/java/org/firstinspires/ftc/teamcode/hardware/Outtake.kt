package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import kotlin.math.abs

class Outtake(hardwareMap: HardwareMap) {
    private val outtakeMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "launcher")

    init {
        outtakeMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        outtakeMotor.setVelocityPIDFCoefficients(
            OuttakeConstants.SHOOTER_KP,
            OuttakeConstants.SHOOTER_KI,
            OuttakeConstants.SHOOTER_KD,
            OuttakeConstants.SHOOTER_KS
        )
        outtakeMotor.power = 0.0
    }

    var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    var velocityAdjustmentFactor = 0.0
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

        if (debugging) {
            // takes 3 ms
            outtakeMotor.setVelocityPIDFCoefficients(
                OuttakeConstants.SHOOTER_KP,
                OuttakeConstants.SHOOTER_KI,
                OuttakeConstants.SHOOTER_KD,
                OuttakeConstants.SHOOTER_KS
            )
        }
        if (active) {
            outtakeMotor.velocity = targetVelocity + velocityAdjustmentFactor
        } else {
            outtakeMotor.power = 0.0
        }
        telemetry.addData("Outtake active", active)
        val velocity = outtakeMotor.velocity
        telemetry.addData("Outtake velocity", velocity)
        telemetry.addData("Outtake target velocity", targetVelocity + velocityAdjustmentFactor)
        telemetry.addData("Outtake velocity adjustment factor", velocityAdjustmentFactor)
        telemetry.addData("Outtake status", if (active && canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(velocity: Double? = null): Boolean {
        return abs(
            targetVelocity + velocityAdjustmentFactor - (velocity ?: outtakeMotor.velocity)
        ) < OuttakeConstants.ALLOWANCE
    }

    fun toggle() {
        active = !active
    }
}