package org.firstinspires.ftc.teamcode.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.IntakeConstants


class Intake(hardwareMap: HardwareMap) {
    private val intakeMotor = hardwareMap.dcMotor["intakeMotor"]

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

    fun periodic() {
        intakeMotor.power = if (active) IntakeConstants.ON_POWER else IntakeConstants.OFF_POWER
    }
}