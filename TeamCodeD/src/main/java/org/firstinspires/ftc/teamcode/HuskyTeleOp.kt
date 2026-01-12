package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp
class HuskyTeleOp : OpMode() {
    override fun init() {
        telemetry.addData("Status", "Initialized 2");
        telemetry.update();
    }

    override fun loop() {
        TODO("Not yet implemented")
    }
}