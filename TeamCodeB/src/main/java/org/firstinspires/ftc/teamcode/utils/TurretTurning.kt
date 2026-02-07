package org.firstinspires.ftc.teamcode

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.teamcode.hardware.Camera
import org.firstinspires.ftc.teamcode.opmode.Paths
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import com.huskyteers19516.shared.Alliance;


@TeleOp(name = "Launcher_Rotator_Only")
class LauncherRotator : OpMode() {

    private val targetX = 0.0
    private val targetY = 0.0

    private val kP = 0.02
    private val kD = 0.002
    private var lastError = 0.0

    private val ticksPerDegree = 1400.0 / 360.0
    private lateinit var turretMotor: DcMotorEx
    private lateinit var follower: Follower
    private lateinit var paths: Paths

    private lateinit var camera: Camera

    private val goal= Pose(144.0,144.0,36.0)



    override fun init() {
        follower = Constants.createFollower(hardwareMap)
        follower.setStartingPose(Pose(72.0, 72.0))
        turretMotor = hardwareMap.get(DcMotorEx::class.java, "turretMotor")
        camera = hardwareMap.get(camera::class.java,"BackupCamara")

        turretMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        paths.buildPaths(Alliance.RED)
    }

    override fun loop() {


        val currentPose = follower.pose

        var targetAngle = (Paths.calculateAimHeading(currentPose, paths.goalLocation)).toInt()
        turretMotor.targetPosition = targetAngle


        val targetPosition = goal



        val ticksneeded = targetAngle * ticksPerDegree

        val error = normalizeAngle(targetAngle - ticksneeded)

        val power: Double


        if (Math.abs(error) > 0.5) {
            val derivative = error - lastError
            power = (error * kP) + (derivative * kD)
        } else {
            power = 0.0
        }

        turretMotor.power = Range.clip(power, -0.8, 0.8)
        lastError = error

        telemetry.addData("Current Angle", "%.2f".format(turretMotor.currentPosition))
        telemetry.addData("Target Angle", "%.2f".format(targetAngle))
        telemetry.update()
    }

    private fun normalizeAngle(angle: Double): Double {
        var degrees = angle % 360
        if (degrees > 270) degrees -= 360
        if (degrees <= -270) degrees += 360
        return degrees
    }
}