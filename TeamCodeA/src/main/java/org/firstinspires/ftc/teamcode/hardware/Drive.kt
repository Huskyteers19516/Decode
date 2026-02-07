package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.huskyteers19516.shared.hl
import com.pedropathing.ftc.drivetrains.Mecanum
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import org.firstinspires.ftc.teamcode.constants.DriveConstants
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.pedroPathing.Drawing

class Drive(private val hardwareMap: HardwareMap) {
    val follower = Constants.createFollower(hardwareMap)

    var throttle = DriveConstants.NORMAL_MODE_SPEED
    var isRobotCentric = DriveConstants.DEFAULT_DRIVE_MODE_IS_ROBOT_CENTRIC

    fun resetOrientation() {
        follower.pose = Pose()
    }

    fun periodic(telemetry: TelemetryManager) {
        follower.update()
        writeTelemetry(telemetry, false)
    }

    fun manualPeriodic(
        forward: Double,
        strafe: Double,
        turn: Double,
        telemetry: TelemetryManager,
    ) {
        follower.update()
        follower.setTeleOpDrive(
            forward * throttle, strafe * throttle, turn * throttle, isRobotCentric
        )
        writeTelemetry(telemetry, true)
    }

    val leftFront: DcMotor by lazy {
        hardwareMap.get(DcMotor::class.java, Constants.driveConstants.leftFrontMotorName)
    }

    val rightFront: DcMotor by lazy {
        hardwareMap.get(DcMotor::class.java, Constants.driveConstants.rightFrontMotorName)
    }

    val leftRear: DcMotor by lazy {
        hardwareMap.get(DcMotor::class.java, Constants.driveConstants.leftRearMotorName)
    }

    val rightRear: DcMotor by lazy {
        hardwareMap.get(DcMotor::class.java, Constants.driveConstants.rightRearMotorName)
    }

    fun debugPeriodic(fl: Double, fr: Double, rl: Double, rr: Double) {
        leftFront.power = fl
        rightFront.power = fr
        leftRear.power = rl
        rightRear.power = rr
    }

    fun debugTelemetry(telemetry: TelemetryManager) {
        val drivetrain = follower.drivetrain
        if (drivetrain is Mecanum) {
            drivetrain.motors.forEachIndexed { index, motor ->
                val names = hardwareMap.getNamesOf(motor).joinToString(", ")
                telemetry.hl()
                telemetry.addData("$names Motor", motor.power)
                telemetry.addData("$names Encoder", motor.currentPosition)
                telemetry.addData("$names Velocity", motor.velocity)
                telemetry.addData("$names Is overcurrent", motor.isOverCurrent)
                telemetry.addData("$names Current (amps)", motor.getCurrent(CurrentUnit.AMPS))
            }
        }
    }

    fun writeTelemetry(telemetry: TelemetryManager, manual: Boolean) {
        if (manual) {
            telemetry.addData("Throttle", throttle)
            telemetry.addData(
                "Drive mode", if (isRobotCentric) "Robot centric" else "Field centric"
            )
        } else {
            telemetry.addData("Is Busy", follower.isBusy)
            telemetry.addData("Is Turning", follower.isTurning)
        }
        telemetry.addData("X (in)", follower.pose.x)
        telemetry.addData("Y (in)", follower.pose.y)
        telemetry.addData("Heading (deg)", Math.toDegrees(follower.pose.heading))

        Drawing.drawDebug(follower)

    }
}