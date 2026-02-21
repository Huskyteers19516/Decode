package org.firstinspires.ftc.teamcode.hardware

import android.util.Log
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import kotlin.math.abs

class Outtake(hardwareMap: HardwareMap) {
    private val outtakeMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "outtake")
    private val hoodServo: Servo = hardwareMap.get(Servo::class.java, "hood")
    private val outtakeBlockerServo: Servo = hardwareMap.get(Servo::class.java, "blocker")
    private val turretMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "turret")
    private val transferMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "transfer")

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
        outtakeMotor.direction = DcMotorSimple.Direction.FORWARD



        turretMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        turretMotor.direction = DcMotorSimple.Direction.REVERSE
        turretMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        turretMotor.setPositionPIDFCoefficients(
            OuttakeConstants.TURRET_KP,
        )
        turretMotor.power = 0.5
        turretMotor.targetPosition = 0

        hoodServo.direction = Servo.Direction.REVERSE
        transferMotor.direction = DcMotorSimple.Direction.REVERSE
    }


    var hoodAngle = OuttakeConstants.HOOD_LOW_ANGLE
    var transferActive = false

    fun convertAngleToPosition(angle: Double): Double {
        return (angle - OuttakeConstants.HOOD_LOW_ANGLE) / (OuttakeConstants.HOOD_HIGH_ANGLE - OuttakeConstants.HOOD_LOW_ANGLE)
    }



    var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    var velocityAdjustmentFactor = 0.0
    var shooterActive = false
   
    var turretAutoAiming = false

    fun manualPeriodic(manualPower: Double, telemetry: TelemetryManager) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        outtakeMotor.power = manualPower
        hoodServo.position = convertAngleToPosition(hoodAngle)
        outtakeBlockerServo.position = if (outtakeOpen) OuttakeConstants.BLOCKER_OPEN_POSITION else OuttakeConstants.BLOCKER_CLOSED_POSITION

        transferMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        transferMotor.power = if (transferActive) OuttakeConstants.TRANSFER_ON_POWER else OuttakeConstants.TRANSFER_OFF_POWER


        telemetry.addData("Outtake active", shooterActive)
        telemetry.addData("Outtake power", outtakeMotor.power)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
    }

    fun periodic(telemetry: TelemetryManager,  debugging: Boolean = false, turretAngle: Double? = null) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        turretMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        transferMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        transferMotor.power = if (transferActive) OuttakeConstants.TRANSFER_ON_POWER else OuttakeConstants.TRANSFER_OFF_POWER
        hoodServo.position = convertAngleToPosition(hoodAngle)
        outtakeBlockerServo.position = if (outtakeOpen) OuttakeConstants.BLOCKER_OPEN_POSITION else OuttakeConstants.BLOCKER_CLOSED_POSITION

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
                val targetPosition = turretAngle * OuttakeConstants.TURRET_TICKS_PER_REV
                turretMotor.power = 0.3
                turretMotor.targetPosition = targetPosition.toInt()
            } else {
                turretMotor.targetPosition = 0
            }
            telemetry.addData("Turret current position", turretMotor.currentPosition)
            telemetry.addData("Turret target position", turretMotor.targetPosition)
        }

        if (shooterActive) {
            outtakeMotor.velocity = targetVelocity + velocityAdjustmentFactor
        } else {
            outtakeMotor.velocity = 0.0
        }

        telemetry.addData("Outtake active", shooterActive)
        telemetry.addData("Outtake hood angle", hoodAngle)
        val velocity = outtakeMotor.velocity
        telemetry.addData("Outtake velocity", velocity)
        telemetry.addData("Outtake velocity adjustment factor", velocityAdjustmentFactor)
        telemetry.addData("Outtake status", if ((shooterActive) && canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Hood position", hoodServo.position)
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(velocity: Double? = null): Boolean {
        return abs(
            targetVelocity + velocityAdjustmentFactor - (velocity ?: outtakeMotor.velocity)
        ) < OuttakeConstants.ALLOWANCE
    }

    var outtakeOpen = false

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
