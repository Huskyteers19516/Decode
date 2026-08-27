package org.firstinspires.ftc.teamcodea.opmode;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class TryoutTemplate {
    static double rotateSpeed= 0.3;
    static double spinAngle=360;
    static double holdSeconds =0.5;



}
final double STOP_SPEED =0.0;
final double WHEEL_DIAMETER_MM = 96;
final double ENCODER_TICKS_PER_REV = 537.7;
final double TICKS_PER_MM = (ENCODER_TICKS_PER_REV / (WHEEL_DIAMETER_MM * Math.PI));
final double TRACK_WIDTH_MM = 404;
final double WHEEL_BASE_MM = 330;
final double TOLERANCE_MM = 10;

double robotSpinAngle = MecanumAuto.Config.spinAngle;

private final ElapsedTime driveTimer = new ElapsedTime();

private DcMotor leftFront = null;
private DcMotor leftBack = null;
private DcMotor rightFront = null;
private DcMotor rightBack = null;

public void init(){

}

public void start(){

}


public void loop(){

}

public void stop(){

}
void resetDriveEncoders() {
    leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
}

void setDriveTargets(double leftTicks, double rightTicks) {
    leftFront.setTargetPosition((int) leftTicks);
    leftBack.setTargetPosition((int) leftTicks);
    rightFront.setTargetPosition((int) rightTicks);
    rightBack.setTargetPosition((int) rightTicks);

    leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
}

void setDrivePower(double leftFrontPower, double leftBackPower, double rightFrontPower, double rightBackPower) {
    leftFront.setPower(leftFrontPower);
    leftBack.setPower(leftBackPower);
    rightFront.setPower(rightFrontPower);
    rightBack.setPower(rightBackPower);
}

boolean rotate(double speed, double angle, AngleUnit angleUnit, double holdSeconds) {
    double turnRadiusMm = (TRACK_WIDTH_MM + WHEEL_BASE_MM) / 2;
    double targetMm = angleUnit.toRadians(angle) * turnRadiusMm;

    double leftTargetPosition = -(targetMm * TICKS_PER_MM);
    double rightTargetPosition = targetMm * TICKS_PER_MM;

    setDriveTargets(leftTargetPosition, rightTargetPosition);
    setDrivePower(speed, speed, speed, speed);

    if (Math.abs(leftTargetPosition - leftFront.getCurrentPosition()) > (TOLERANCE_MM * TICKS_PER_MM)) {
        driveTimer.reset();
    }

    return driveTimer.seconds() > holdSeconds;
}