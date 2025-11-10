package org.firstinspires.ftc.teamcodeb.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcodeb.pedroPathing.Constants;

@TeleOp
public class MecanumDirectionDebugger extends OpMode {
    private DcMotorEx frontRight;
    private DcMotorEx frontLeft;
    private DcMotorEx backRight;
    private DcMotorEx backLeft;
    @Override
    public void init() {
        frontRight = hardwareMap.get(DcMotorEx.class, "front_right");
        frontLeft = hardwareMap.get(DcMotorEx.class, "front_left");
        backRight = hardwareMap.get(DcMotorEx.class, "back_right");
        backLeft = hardwareMap.get(DcMotorEx.class, "back_left");
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setDirection(Constants.driveConstants.rightFrontMotorDirection);
        frontLeft.setDirection(Constants.driveConstants.leftFrontMotorDirection);
        backRight.setDirection(Constants.driveConstants.rightRearMotorDirection);
        backLeft.setDirection(Constants.driveConstants.leftRearMotorDirection);
    }

    @Override
    public void loop() {
        frontRight.setPower(gamepad1.a ? 1 : 0);
        backRight.setPower(gamepad1.b ? 1 : 0);
        frontLeft.setPower(gamepad1.x ? 1 : 0);
        backLeft.setPower(gamepad1.y ? 1 : 0);

        telemetry.addLine("If you're doing this correctly, all the velocities should be positive, and all the wheels should go forward");

        telemetry.addData("Front Right Position", frontRight.getCurrentPosition());
        telemetry.addData("Front Right Velocity", frontRight.getVelocity());
        telemetry.addData("Front Left Position", frontLeft.getCurrentPosition());
        telemetry.addData("Front Left Velocity", frontLeft.getVelocity());
        telemetry.addData("Back Right Position", backRight.getCurrentPosition());
        telemetry.addData("Back Right Velocity", backRight.getVelocity());
        telemetry.addData("Back Left Position", backLeft.getCurrentPosition());
        telemetry.addData("Back Left Velocity", backLeft.getVelocity());

    }
}
