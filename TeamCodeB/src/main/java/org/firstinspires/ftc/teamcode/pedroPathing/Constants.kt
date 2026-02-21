package org.firstinspires.ftc.teamcode.pedroPathing

import com.pedropathing.control.FilteredPIDFCoefficients
import com.pedropathing.control.PIDFCoefficients
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
        .mass(12.06556)
        .forwardZeroPowerAcceleration(-33.47)
        .lateralZeroPowerAcceleration(-68.594)
        .translationalPIDFCoefficients(PIDFCoefficients(0.12, 0.0, 0.01, 0.03))
        .headingPIDFCoefficients(PIDFCoefficients(1.3, 0.0, 0.1, 0.03))

//        .drivePIDFCoefficients(FilteredPIDFCoefficients(0.02, 0.0, 0.00001, 0.6, 0.01))

    var pathConstraints: PathConstraints = PathConstraints(0.99, 100.0, 1.0, 1.0)

    var driveConstants: MecanumConstants = MecanumConstants()
        .maxPower(1.0)
        .xVelocity(70.5)
        .yVelocity(50.48)
        .rightFrontMotorName("front_right")
        .rightRearMotorName("back_right")
        .leftRearMotorName("back_left")
        .leftFrontMotorName("front_left")
        .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
        .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
        .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
        .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)

    var localizerConstants: PinpointConstants = PinpointConstants()
        .forwardPodY(-1.25984)
        .strafePodX(-1.77165)
        .distanceUnit(DistanceUnit.INCH)
        .hardwareMapName("pinpoint")
        .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
        .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
        .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED)

    @JvmStatic
    fun createFollower(hardwareMap: HardwareMap?): Follower {
        return FollowerBuilder(followerConstants, hardwareMap)
            .pinpointLocalizer(localizerConstants)
            .pathConstraints(pathConstraints)
            .mecanumDrivetrain(driveConstants)
            .build()
    }
}