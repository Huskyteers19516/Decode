package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.IntakeConstants


class Intake(hardwareMap: HardwareMap) {
    private val intakeMotor = hardwareMap.dcMotor["intake"]

    init {
        intakeMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        intakeMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        intakeMotor.power = 0.0
    }

    var active = false

    fun start() {
        active = true
    }

    fun stop() {
        active = false
    }

    fun teleOpPeriodic(driverStrength: Double, telemetry: TelemetryManager) {
        intakeMotor.power = if (active) IntakeConstants.ON_POWER else driverStrength

        telemetry.addData("Intake active", active)
        telemetry.addData("Intake power", intakeMotor.power)
    }

    fun periodic( telemetry: TelemetryManager) {
        intakeMotor.power = if (active) IntakeConstants.ON_POWER else IntakeConstants.OFF_POWER
        telemetry.addData("Intake active", active)
        telemetry.addData("Intake power", intakeMotor.power)
    }
}