package org.firstinspires.ftc.teamcode.pedroPathing

import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit


object Constants {
    var followerConstants: FollowerConstants = FollowerConstants()
        .mass(13.2449)
        .forwardZeroPowerAcceleration(-23.58)
        .lateralZeroPowerAcceleration(-36.67)

    var pathConstraints: PathConstraints = PathConstraints(0.99, 100.0, 1.0, 1.0)

    var driveConstants: MecanumConstants = MecanumConstants()
        .maxPower(1.0)
        .xVelocity(56.9)
        .yVelocity(48.94)
        .rightFrontMotorName("front_right")
        .rightRearMotorName("back_right")
        .leftRearMotorName("back_left")
        .leftFrontMotorName("front_left")
        .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
        .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
        .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
        .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)

    var localizerConstants: PinpointConstants = PinpointConstants()
        .forwardPodY(6.7905)
        .strafePodX(-2.928)
        .distanceUnit(DistanceUnit.INCH)
        .hardwareMapName("pinpoint")
        .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
        .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
        .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)

    @JvmStatic
    fun createFollower(hardwareMap: HardwareMap?): Follower {
        return FollowerBuilder(followerConstants, hardwareMap)
            .pinpointLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build()
    }
}