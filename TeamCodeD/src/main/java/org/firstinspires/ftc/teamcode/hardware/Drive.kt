package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.pedropathing.geometry.Pose
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.pedroPathing.Constants
import org.firstinspires.ftc.teamcode.pedroPathing.Drawing

class Drive(hardwareMap: HardwareMap) {
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