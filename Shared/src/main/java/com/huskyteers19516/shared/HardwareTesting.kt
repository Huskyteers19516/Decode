package com.huskyteers19516.shared

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

@TeleOp(name = "Hardware Testing", group = "Testing")
class HardwareTesting : OpMode() {
    lateinit var crServos: List<CRServo>
    lateinit var servos: List<Servo>
    lateinit var motors: List<DcMotorEx>
    var currentHardwareIndex: Int = 0
    var controlModeIndex: Int = 0

    var joystickMode = false

    override fun init() {
        hardwareMap.logDevices()
        crServos = hardwareMap.getAll(CRServo::class.java)
        servos = hardwareMap.getAll(Servo::class.java)
        motors = hardwareMap.getAll(DcMotorEx::class.java)
        telemetry.addData("Found ", "${crServos.size} CR Servos.")
        telemetry.addData("Found ", "${servos.size} Servos.")
        telemetry.addData("Found ", "${motors.size} DC Motors.")
        telemetry.update()
    }

    var temporaryValue = 0.0

    override fun loop() {
        // Control Mode Selection
        val controlMode = ControlMode.entries[controlModeIndex]
        telemetry.addLine("Press start to change control mode.")
        telemetry.addData("Currently testing", controlMode.toString())

        if (gamepad1.startWasPressed()) {
            controlModeIndex =
                (controlModeIndex - 1 + ControlMode.entries.size) % ControlMode.entries.size
            currentHardwareIndex = 0
        }

        val hardwareList: List<HardwareDevice> = when (controlMode) {
            ControlMode.CR_SERVO -> crServos
            ControlMode.SERVO -> servos
            ControlMode.MOTOR -> motors
        }
        if (hardwareList.isEmpty()) {
            telemetry.addLine("No " + controlMode.modeName + "s found in hardware map!")
            telemetry.update()
            return
        }


        // Hardware device selection
        telemetry.addData(controlMode.modeName + " Count", hardwareList.size)
        for (device in hardwareList) {
            telemetry.addData(hardwareMap.getNamesOf(device).toString(), device.connectionInfo)
        }


        telemetry.addLine("Press the left and right bumpers to cycle " + controlMode.modeName + "s.")
        if (gamepad1.leftBumperWasPressed()) {
            currentHardwareIndex =
                (currentHardwareIndex - 1 + hardwareList.size) % hardwareList.size
        } else if (gamepad1.rightBumperWasPressed()) {
            currentHardwareIndex = (currentHardwareIndex + 1) % hardwareList.size
        }

        // Hardware device control
        val currentDevice: HardwareDevice = hardwareList[currentHardwareIndex]


        telemetry.addLine("Currently controlling " + hardwareMap.getNamesOf(currentDevice))


        telemetry.addLine("Press X to change joystick mode.")
        if (gamepad1.xWasPressed()) {
            joystickMode = !joystickMode
            if (!joystickMode) {
                if (controlMode == ControlMode.SERVO) {
                    val servo = currentDevice as Servo
                    temporaryValue = currentDevice.position
                } else {
                    val motorOrServo = currentDevice as DcMotorSimple
                    temporaryValue = currentDevice.power
                }
            }
        }

        telemetry.addData("Joystick Mode", joystickMode)

        telemetry.addLine(if (joystickMode) "Move the left stick up and down to control the position or power" else "Use the dpad to control the temporary value, then press A to actually set the position or power to that temporary value")
        if (!joystickMode) {
            telemetry.addData("Temporary Value", temporaryValue)
            if (gamepad1.dpadUpWasPressed()) {
                temporaryValue += 0.1
            } else if (gamepad1.dpadDownWasPressed()) {
                temporaryValue -= 0.1
            } else if (gamepad1.dpadRightWasPressed()) {
                temporaryValue += 0.01
            } else if (gamepad1.dpadLeftWasPressed()) {
                temporaryValue -= 0.01
            }
            temporaryValue = temporaryValue.coerceIn(-1.0, 1.0)
        }

        val leftStickY = gamepad1.left_stick_y.toDouble()
        if (controlMode == ControlMode.SERVO) {
            val servo = currentDevice as Servo
            telemetry.addData("Current position", currentDevice.position)
            if (joystickMode) {
                servo.position = leftStickY * 2 - 1  // Map -1 to 1 range to 0 to 1
            } else if (gamepad1.aWasPressed()) {
                servo.position = temporaryValue
            }
        } else {
            val motorOrServo = currentDevice as DcMotorSimple
            telemetry.addData("Current power", currentDevice.power)
            if (joystickMode) {
                motorOrServo.power = leftStickY
            } else if (gamepad1.aWasPressed()) {
                motorOrServo.power = temporaryValue
            }

            if (controlMode == ControlMode.MOTOR) {
                val motor = currentDevice as DcMotorEx
                telemetry.addData("Current position (ticks)", motor.currentPosition)
                telemetry.addData("Current velocity (ticks/sec)", motor.velocity)
                telemetry.addData(
                    "Current velocity (deg/sec)",
                    motor.getVelocity(AngleUnit.DEGREES)
                )
                telemetry.addData(
                    "Current velocity (rad/sec)",
                    motor.getVelocity(AngleUnit.RADIANS)
                )
                telemetry.addData("Current current (amps)", motor.getCurrent(CurrentUnit.AMPS))
            }
        }
        telemetry.update()
    }

    private enum class ControlMode(val modeName: String) {
        CR_SERVO("Continuous Rotation Servo"),
        SERVO("Standard Servo"),
        MOTOR("DC Motor");
    }
}
