package org.firstinspires.ftc.teamcode.opmode

import com.bylazar.telemetry.PanelsTelemetry
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake

@TeleOp(name = "System Testing", group = "Testing")
class SystemTesting : OpMode() {
    lateinit var telemetryM: TelemetryManager
    lateinit var intake: Intake
    lateinit var flippers: Flippers
    lateinit var outtake: Outtake
    override fun init() {
        telemetryM = PanelsTelemetry.telemetry
        intake = Intake(hardwareMap)
        flippers = Flippers(hardwareMap)
        outtake = Outtake(hardwareMap)
    }

    override fun loop() {
        telemetryM.addLine("System Testing")
        telemetryM.addLine("Use the left stick to control the intake")
        telemetryM.addLine("Use the right stick to control the outtake")
        telemetryM.addLine("A for flipper A, B for flipper B, X for flipper C")
        telemetryM.addLine("YOU'RE WELCOME")

        intake.manualPeriodic(-gamepad1.left_stick_y.toDouble(), telemetryM)
        outtake.manualPeriodic(-gamepad1.right_stick_y.toDouble(), telemetryM)
        flippers.periodic(telemetryM)
        if (gamepad1.a) flippers.raiseFlipper(Flippers.Flipper.A) else flippers.lowerFlipper(Flippers.Flipper.A)
        if (gamepad1.b) flippers.raiseFlipper(Flippers.Flipper.B) else flippers.lowerFlipper(Flippers.Flipper.B)
        if (gamepad1.x) flippers.raiseFlipper(Flippers.Flipper.C) else flippers.lowerFlipper(Flippers.Flipper.C)
        telemetryM.update()
    }

}