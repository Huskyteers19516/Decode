package org.firstinspires.ftc.teamcode.hardware

import android.util.Log
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.*
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import kotlin.math.abs
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait

class Outtake(hardwareMap: HardwareMap) {
    private val outtakeMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "outtake")
    private val hoodServo: Servo = hardwareMap.get(Servo::class.java, "hood")
    private val outtakeBlockerServo: Servo = hardwareMap.get(Servo::class.java, "blocker")
    private val turretMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "turret")

    init {
        outtakeMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        outtakeMotor.setVelocityPIDFCoefficients(
            OuttakeConstants.SHOOTER_KP,
            OuttakeConstants.SHOOTER_KI,
            OuttakeConstants.SHOOTER_KD,
            OuttakeConstants.SHOOTER_KS
        )
        outtakeMotor.power = 0.0

        turretMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        turretMotor.direction = DcMotorSimple.Direction.REVERSE
        turretMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        turretMotor.setPositionPIDFCoefficients(
            OuttakeConstants.TURRET_KP,
        )
        turretMotor.power = 0.5
        turretMotor.targetPosition = 0
    }

    var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    var velocityAdjustmentFactor = 0.0
    var shooterActive = false
   
    var turretAutoAiming = false
    var turretManualAiming= false
    var turretleft = false
    var turretright = false;
    var shootOne  = false;
    var stopShoot = false;

    private var aprilTagAdjustment = 0

    fun manualPeriodic(manualPower: Double, telemetry: TelemetryManager) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        outtakeMotor.power = manualPower
        telemetry.addData("Outtake active", shooterActive)
        telemetry.addData("Outtake power", outtakeMotor.power)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
    }

    fun periodic(telemetry: TelemetryManager,  debugging: Boolean = false, turretAngle: Double? = null, goalTag: AprilTagDetection? = null) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        if (debugging) {
            // takes 3 ms
            outtakeMotor.setVelocityPIDFCoefficients(
                OuttakeConstants.SHOOTER_KP,
                OuttakeConstants.SHOOTER_KI,
                OuttakeConstants.SHOOTER_KD,
                OuttakeConstants.SHOOTER_KS
            )
            turretMotor.setPositionPIDFCoefficients(
                OuttakeConstants.TURRET_KP,
            )
        }
        if (turretAngle != null) {
            if (turretAutoAiming) {
                aprilTagAdjustment = goalTag?.ftcPose?.bearing?.toInt() ?: 0

                val targetPosition = (turretAngle + aprilTagAdjustment) * OuttakeConstants.TURRET_TICKS_PER_REV
                turretMotor.power = 0.3
                turretMotor.targetPosition = targetPosition.toInt()
            } else if(turretManualAiming) {
                turretAutoAiming= false;
            }else{
                turretMotor.targetPosition = 0
            }
            telemetry.addData("Turret current position", turretMotor.currentPosition)
            telemetry.addData("Turret target position", turretMotor.targetPosition)
        }

        if(turretleft){
            turretMotor.power=0.2
        }
        if(turretright){
            turretMotor.power = -0.2
        }

        if(shootOne){
            outtakeMotor.velocity= targetVelocity;
        }
        if(stopShoot){
            outtakeMotor.velocity=0.0;
        }

        if (shooterActive) {
            outtakeMotor.velocity = targetVelocity + velocityAdjustmentFactor
        } else {
            outtakeMotor.power = 0.0
        }
        telemetry.addData("Outtake active", shooterActive)
        val velocity = outtakeMotor.velocity
        telemetry.addData("Outtake velocity", velocity)
        telemetry.addData("Outtake target velocity", targetVelocity + velocityAdjustmentFactor)
        telemetry.addData("Outtake velocity adjustment factor", velocityAdjustmentFactor)
        telemetry.addData("Outtake status", if (shooterActive && canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(velocity: Double? = null): Boolean {
        return abs(
            targetVelocity + velocityAdjustmentFactor - (velocity ?: outtakeMotor.velocity)
        ) < OuttakeConstants.ALLOWANCE
    }

    fun openOuttakeBlocker() {
        outtakeBlockerServo.position = 1.0
    }
    fun closeOuttakeBlocker() {
        outtakeBlockerServo.position = 0.0
    }

    fun shoot() = sequence(
        wait(::canShoot),
        exec(::openOuttakeBlocker),
        wait(0.25),
        exec { outtakeMotor.velocity = targetVelocity + velocityAdjustmentFactor },
    )
    fun stopshoot() = sequence(
        exec(::closeOuttakeBlocker),
        wait(0.25),
        exec { outtakeMotor.velocity = 0.0 },
    )

    fun toggle() {
        shooterActive = !shooterActive
    }

    companion object {
        // List of pairs with the first being the distance in inches, the second being the target velocity
        val knownValues = listOf(
            25.0 to 1100.0,
            11.0 to 1000.0,
            28.75 to 1200.0,
            46.0 to 1300.0,
            64.0 to 1500.0,
            78.75 to 1620.0
        )

        fun getBestTargetVelocity(range: Double): Double {
            Log.d("HuskyTeleOp", "using range: $range in")

            val sorted = knownValues.sortedBy { it.first }

            // exact match
            for ((d, v) in sorted) if (d == range) {
                Log.d("HuskyTeleOp", "Found $v")
                return v
            }

            // clamp to ends
            if (range <= sorted.first().first) return sorted.first().second
            if (range >= sorted.last().first) return sorted.last().second

            // find interval and linearly interpolate
            for (i in 0 until sorted.size - 1) {
                val (d1, v1) = sorted[i]
                val (d2, v2) = sorted[i + 1]
                if (range in d1..d2) {
                    val t = (range - d1) / (d2 - d1)
                    Log.d("HuskyTeleOp", "${v1 + t * (v2 - v1)}")

                    return v1 + t * (v2 - v1)
                }
            }

            // fallback (shouldn't be reached)
            return sorted.last().second
        }

    }

}
