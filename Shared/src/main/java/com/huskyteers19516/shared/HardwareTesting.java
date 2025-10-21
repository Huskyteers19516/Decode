package com.huskyteers19516.shared;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TeleOp
public class HardwareTesting extends OpMode {
    List<CRServo> crServos;
    List<Servo> servos;
    List<DcMotorEx> motors;
    int currentHardwareIndex = 0;

    @Override
    public void init() {
        hardwareMap.logDevices();
        crServos = hardwareMap.getAll(CRServo.class);
        servos = hardwareMap.getAll(Servo.class);
        motors = hardwareMap.getAll(DcMotorEx.class);
        telemetry.addData("Found ", crServos.size() + " CR Servos.");
        telemetry.addData("Found ", servos.size() + " Servos.");
        telemetry.addData("Found ", motors.size() + " DC Motors.");
    }

    private enum ControlMode {
        CR_SERVO("Continuous Rotation Servo"),
        SERVO("Standard Servo"),
        MOTOR("DC Motor");
         ControlMode(final String modeName) {
            this.modeName = modeName;
        }
        private final String modeName;
    }

    ArrayList<ControlMode> controlModes = new ArrayList<>(Arrays.asList(ControlMode.values()));

    int controlModeIndex = 0;

    @Override
    public void loop() {
        // Control Mode Selection
        ControlMode controlMode = controlModes.get(controlModeIndex);
        telemetry.addLine("Press left and right on the D-pad to change control mode.");
        telemetry.addData("Currently testing", controlMode.toString());
        if (gamepad1.dpadLeftWasPressed()) {
            controlModeIndex = (controlModeIndex - 1 + controlModes.size()) % controlModes.size();
            currentHardwareIndex = 0;
        } else if (gamepad1.dpadRightWasPressed()) {
            controlModeIndex = (controlModeIndex + 1) % controlModes.size();
            currentHardwareIndex = 0;
        }

        List<? extends HardwareDevice> hardwareList;
        switch (controlMode) {
            case CR_SERVO:
                hardwareList = crServos;
                break;
            case SERVO:
                hardwareList = servos;
                break;
            case MOTOR:
                hardwareList = motors;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + controlMode);
        }
        if (hardwareList.isEmpty()) {
            telemetry.addLine("No " + controlMode.modeName + "s found in hardware map!");
            telemetry.update();
            return;
        }


        // Hardware device selection
        telemetry.addData( controlMode.modeName + " Count", hardwareList.size());
        for (HardwareDevice device : hardwareList) {
            telemetry.addData(hardwareMap.getNamesOf(device).toString(), device.getConnectionInfo());
        }
        telemetry.addLine("Press D-pad up and down to cycle " + controlMode.modeName + "s.");
        if (gamepad1.dpadDownWasPressed()) {
            currentHardwareIndex = (currentHardwareIndex - 1 + hardwareList.size()) % hardwareList.size();
        } else if (gamepad1.dpadUpWasPressed()) {
            currentHardwareIndex = (currentHardwareIndex + 1) % hardwareList.size();
        }
        // Hardware device control
        HardwareDevice currentDevice = hardwareList.get(currentHardwareIndex);
        telemetry.addLine("Currently controlling " + hardwareMap.getNamesOf(currentDevice));
        double leftStickY = gamepad1.left_stick_y;
        if (controlMode == ControlMode.SERVO) {
            telemetry.addData("Current position", leftStickY);
            Servo servo = (Servo) currentDevice;
            double position = Math.max(0, leftStickY); // Map -1 to 1 range to 0 to 1
            servo.setPosition(position);
        } else {
            DcMotorSimple motorOrServo = (DcMotorSimple) currentDevice;
            telemetry.addData("Current power", leftStickY);
            motorOrServo.setPower(leftStickY);
            if (controlMode == ControlMode.MOTOR) {
                DcMotorEx motor = (DcMotorEx) currentDevice;
                telemetry.addData("Current velocity (ticks/sec)", motor.getVelocity());
                telemetry.addData("Current velocity (deg/sec)", motor.getVelocity(AngleUnit.DEGREES));
                telemetry.addData("Current velocity (rad/sec)", motor.getVelocity(AngleUnit.RADIANS));
                telemetry.addData("Current current (amps)", motor.getCurrent(CurrentUnit.AMPS));
            }
        }
        telemetry.update();
    }
}
