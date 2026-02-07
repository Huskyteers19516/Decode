package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp
public class MoveMotorForward extends OpMode {
    private DcMotor motor;

    @Override
    public void init() {
        // Initialize motor here
        motor = hardwareMap.get(DcMotorEx.class, "flipper");
    }

    @Override
    public void loop() {
        motor.setPower(1);
    }
}
