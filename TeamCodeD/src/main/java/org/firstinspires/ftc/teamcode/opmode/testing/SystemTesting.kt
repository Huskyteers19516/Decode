package org.firstinspires.ftc.teamcode.opmode.testing

import com.bylazar.telemetry.PanelsTelemetry
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.hardware.Flippers
import org.firstinspires.ftc.teamcode.hardware.Intake
import org.firstinspires.ftc.teamcode.hardware.Outtake
import org.firstinspires.ftc.teamcode.utils.Slot

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

    val velocityMode = true

    override fun loop() {
        telemetryM.addLine("System Testing")
        telemetryM.addLine("Use the left stick to control the intake")
        telemetryM.addLine("A for flipper A, B for flipper B, X for flipper C")

        telemetryM.addData(
            "Outtake mode",
            if (velocityMode) "Constant velocity mode" else "Manual power mode. Use right stick to control the outtake"
        )

        intake.manualPeriodic(-gamepad1.left_stick_y.toDouble(), telemetryM)
        if (velocityMode) {
            telemetryM.addLine("Use dpad to control the outtake velocity, Y to toggle")
            outtake.periodic(telemetryM)
            if (gamepad1.dpadUpWasPressed()) {
                outtake.targetVelocity += 100
            } else if (gamepad1.dpadDownWasPressed()) {
                outtake.targetVelocity -= 100
            } else if (gamepad1.dpadLeftWasPressed()) {
                outtake.targetVelocity -= 20
            } else if (gamepad1.dpadRightWasPressed()) {
                outtake.targetVelocity += 20
            }
            if (gamepad1.yWasPressed()) outtake.toggle()
        } else {
            telemetryM.addLine("Use the right stick to control the outtake")
            outtake.manualPeriodic(-gamepad1.right_stick_y.toDouble(), telemetryM)
        }
        flippers.periodic(telemetryM)


        if (gamepad1.a) flippers.raiseFlipper(Slot.A) else flippers.lowerFlipper(Slot.A)
        if (gamepad1.b) flippers.raiseFlipper(Slot.B) else flippers.lowerFlipper(Slot.B)
        if (gamepad1.x) flippers.raiseFlipper(Slot.C) else flippers.lowerFlipper(Slot.C)
        telemetryM.update(telemetry)
    }

}