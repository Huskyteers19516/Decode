package org.firstinspires.ftc.teamcode.hardware

import android.util.Log
import android.util.Size
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.constants.CameraConstants
import org.firstinspires.ftc.teamcode.utils.Alliance
import org.firstinspires.ftc.teamcode.utils.Motif
import org.firstinspires.ftc.teamcode.utils.hl
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import java.util.concurrent.TimeUnit

class Camera(hardwareMap: HardwareMap) {
    val aprilTagProcessor: AprilTagProcessor =
        AprilTagProcessor.Builder().setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES).build()
    val visionPortal: VisionPortal =
        VisionPortal.Builder().setCamera(hardwareMap.get(CameraName::class.java, "Webcam 1"))
            .setCameraResolution(Size(640, 480)).enableLiveView(true).setAutoStopLiveView(true)
            .setAutoStartStreamOnBuild(true).addProcessor(aprilTagProcessor).setShowStatsOverlay(true).build()

    fun waitForCamera(telemetry: TelemetryManager, updateTelemetry: () -> Unit) = sequence(
        exec {
            telemetry.addLine("Waiting for camera")
            updateTelemetry()
        },
        wait { visionPortal.cameraState == VisionPortal.CameraState.STREAMING },
        exec {
            setControls()
            telemetry.addLine("Camera initialized")
            updateTelemetry()
        },
    )

    fun setControls() {
        val exposure = visionPortal.getCameraControl(ExposureControl::class.java)
        exposure.mode = ExposureControl.Mode.Manual
        exposure.setExposure(CameraConstants.EXPOSURE_MS, TimeUnit.MILLISECONDS)
        val gain = visionPortal.getCameraControl(GainControl::class.java)
        Log.d(TAG, "Setting gain worked: ${gain.setGain(CameraConstants.GAIN)}")
    }

    fun debugTelemetry(telemetry: TelemetryManager) {
        aprilTagProcessor.detections.forEach { detection ->
            telemetry.addData(
                "Tag ID ${detection.id}", "X: %.2f in, Y: %.2f in, Z: %.2f in, Bearing: %.2f deg".format(
                    detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z, detection.ftcPose.bearing
                )
            )
            telemetry.hl()
        }
    }

    fun getTargetTag(alliance: Alliance): AprilTagDetection? {
        return aprilTagProcessor.detections?.firstOrNull {
            it.metadata.name == (if (alliance == Alliance.RED) "RedTarget" else "BlueTarget")
        }
    }

    fun getObelisk(): Motif? {
        aprilTagProcessor.detections?.forEach {
            Motif.entries.forEach { motif ->
                if (motif.name == "Obelisk_${it.metadata.name}") {
                    return motif
                }
            }
        }
        return null
    }

    companion object {
        const val TAG = "Camera"
    }
}