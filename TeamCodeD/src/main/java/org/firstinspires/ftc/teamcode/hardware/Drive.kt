package org.firstinspires.ftc.teamcode.hardware

import android.util.Size
import com.bylazar.telemetry.TelemetryManager
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.pedroPathing.Drawing
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import org.openftc.easyopencv.OpenCvWebcam

class Drive(hardwareMap: HardwareMap) {
    val aprilTagProcessor = AprilTagProcessor.Builder()
        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
        .build()
    val visionPortal = VisionPortal.Builder()
        .setCamera(hardwareMap.get(CameraName::class.java, "Webcam 1"))
        .setCameraResolution(Size(640, 480))
        .enableLiveView(true)
        .setAutoStopLiveView(true)
        .setAutoStartStreamOnBuild(true)
        .addProcessor(aprilTagProcessor)
        .build()

    enum class State {
        NOT_FOUND,
        IN_PROGRESS,
        DONE
    }

    fun orientTowardsAprilTag(alliance: Alliance): State {
        val goalTag = aprilTagProcessor.detections.find {
            it.metadata.name == (if (alliance == Alliance.BLUE) "BlueTarget" else "RedTarget")
        }
        goalTag?.let {
            val headingError = it.ftcPose.bearing
            follower.setTeleOpDrive(0.0, 0.0, headingError)
            return if (headingError < 5) {
                State.DONE
            } else {
                State.IN_PROGRESS
            }
        }
        return State.NOT_FOUND
    }

    val follower = Constants.createFollower(hardwareMap)

    var throttle = 1.0
    var isRobotCentric = false

    fun resetOrientation() {
        follower.pose = Pose()
    }

    fun periodic(telemetry: TelemetryManager) {
        follower.update()
        writeTelemetry(telemetry, false)
    }

    fun manualPeriodic(forward: Double, strafe: Double, turn: Double, telemetry: TelemetryManager) {
        follower.update()
        follower.setTeleOpDrive(
            forward * throttle,
            strafe * throttle,
            turn * throttle,
            isRobotCentric
        )
        writeTelemetry(telemetry, true)
    }

    fun writeTelemetry(telemetry: TelemetryManager, manual: Boolean) {
        telemetry.addData("X (in)", follower.pose.x)
        telemetry.addData("Y (in)", follower.pose.y)
        telemetry.addData("Heading (deg)", Math.toDegrees(follower.pose.heading))
        if (manual ){
            telemetry.addData("Throttle", throttle)
            telemetry.addData(
                "Drive mode",
                if (isRobotCentric) "Robot centric" else "Field centric"
            )
        } else {
            telemetry.addData("Is Busy", follower.isBusy)
        }

        Drawing.drawDebug(follower)
    }
}