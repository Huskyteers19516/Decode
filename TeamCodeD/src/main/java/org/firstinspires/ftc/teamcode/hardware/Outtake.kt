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

    private var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    private var active = false


    fun periodic(telemetry: TelemetryManager) {
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
        telemetry.addData("Outtake power", outtakeMotor.power)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
        telemetry.addData("Outtake status", if (canShoot()) "CAN SHOOT" else "NOT READY")
    }

    fun canShoot(): Boolean {
        return abs(targetVelocity - outtakeMotor.velocity) < OuttakeConstants.ALLOWANCE
    }

    fun getTargetVelocity(): Double {
        return targetVelocity
    }

    fun setTargetVelocity(velocity: Double) {
        targetVelocity = velocity
    }


    fun start() {
        active = true
    }

    fun stop() {
        active = false
    }

    fun toggle() {
        active = !active
    }
}