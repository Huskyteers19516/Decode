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

        if (debugging) {
            // takes 3 ms
            outtakeMotor.setVelocityPIDFCoefficients(
                OuttakeConstants.KP,
                OuttakeConstants.KI,
                OuttakeConstants.KD,
                OuttakeConstants.KS
            )
        }
        if (active) {
            outtakeMotor.velocity = targetVelocity
        } else {
            outtakeMotor.power = 0.0
        }
        telemetry.addData("Outtake active", active)
        val velocity = outtakeMotor.velocity
        telemetry.addData("Outtake velocity", velocity)
        telemetry.addData("Outtake target velocity", velocity)
        telemetry.addData("Outtake status", if (active && canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(velocity: Double? = null): Boolean {
        return abs(targetVelocity - (velocity ?: outtakeMotor.velocity)) < OuttakeConstants.ALLOWANCE
    }

    fun toggle() {
        active = !active
    }
}