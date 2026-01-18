package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.hardware.ColorSensors

@TeleOp(name = "System Testing", group = "Testing")
class ColorSensorTesting : OpMode() {
    lateinit var telemetryM: TelemetryManager
    lateinit var colorSensors: ColorSensors
    override fun init() {
        telemetryM = PanelsTelemetry.telemetry
        colorSensors = ColorSensors(hardwareMap)
    }

    override fun loop() {
        telemetryM.addLine("Color Sensor Testing")

        colorSensors.telemetry(telemetryM)

        telemetryM.update(telemetry)
    }

}