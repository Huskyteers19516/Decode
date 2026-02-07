package org.firstinspires.ftc.teamcode.pedroPathing

import com.pedropathing.follower.Follower
import com.pedropathing.follower.FollowerConstants
import com.pedropathing.ftc.FollowerBuilder
import com.pedropathing.ftc.drivetrains.MecanumConstants
import com.pedropathing.ftc.localization.Encoder
import com.pedropathing.ftc.localization.constants.DriveEncoderConstants
import com.pedropathing.ftc.localization.constants.PinpointConstants
import com.pedropathing.paths.PathConstraints
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

object Constants {
    var followerConstants: FollowerConstants = FollowerConstants()
        .mass(3.5)
        .forwardZeroPowerAcceleration(-41.3351)
        .lateralZeroPowerAcceleration(-72.43)
    var driveConstants: MecanumConstants = MecanumConstants()
        .maxPower(1.0)
        .rightFrontMotorName("front_right")
        .rightRearMotorName("back_right")
        .leftRearMotorName("back_left")
        .leftFrontMotorName("front_left")
        .leftFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
        .leftRearMotorDirection(DcMotorSimple.Direction.FORWARD)
        .rightFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
        .rightRearMotorDirection(DcMotorSimple.Direction.REVERSE)
        .xVelocity(61.0)
        .yVelocity(48.6)

    var pathConstraints: PathConstraints = PathConstraints(0.99, 100.0, 1.0, 1.0)
    var driveEncoderConstants: DriveEncoderConstants = DriveEncoderConstants()
        .rightFrontMotorName("front_right")
        .rightRearMotorName("back_right")
        .leftRearMotorName("back_left")
        .leftFrontMotorName("front_left")
        .leftFrontEncoderDirection(Encoder.FORWARD)
        .leftRearEncoderDirection(Encoder.FORWARD)
        .rightFrontEncoderDirection(Encoder.REVERSE)
        .rightRearEncoderDirection(Encoder.REVERSE)
        .robotWidth(16.299)
        .robotLength(9.449)
        .forwardTicksToInches(0.00561)
        .strafeTicksToInches(0.00599)
        .turnTicksToInches(0.01142)

    var localizerConstants: PinpointConstants = PinpointConstants()
        .forwardPodY(-3.311024)
        .strafePodX(-2.523622)
        .distanceUnit(DistanceUnit.INCH)
        .hardwareMapName("pinpoint")
        .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
        .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
        .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)

    @JvmStatic
    fun createFollower(hardwareMap: HardwareMap): Follower {
        return FollowerBuilder(followerConstants, hardwareMap)
            .driveEncoderLocalizer(driveEncoderConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build()
    }
}
