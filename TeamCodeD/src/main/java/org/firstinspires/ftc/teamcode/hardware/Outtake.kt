package org.firstinspires.ftc.teamcode.hardware

import android.util.Log
import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.OuttakeConstants
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.pow

class Outtake(hardwareMap: HardwareMap) {
    private val outtakeMotor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "outtake")

    init {
        outtakeMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        outtakeMotor.setVelocityPIDFCoefficients(
            OuttakeConstants.KP,
            OuttakeConstants.KI,
            OuttakeConstants.KD,
            OuttakeConstants.KS
        )
        outtakeMotor.power = 0.0
    }

    var targetVelocity = OuttakeConstants.DEFAULT_TARGET_VELOCITY;
    var active = false

    fun manualPeriodic(manualPower: Double, telemetry: TelemetryManager) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        outtakeMotor.power = manualPower
        telemetry.addData("Outtake active", active)
        telemetry.addData("Outtake power", outtakeMotor.power)
        telemetry.addData("Outtake velocity", outtakeMotor.velocity)
    }

    fun periodic(telemetry: TelemetryManager, debugging: Boolean = false) {
        outtakeMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        if (debugging) {
            // takes 3 ms
            outtakeMotor.setVelocityPIDFCoefficients(
                OuttakeConstants.KP,
                OuttakeConstants.KI,
                OuttakeConstants.KD,
                OuttakeConstants.KS
            )
        }
        if (active) {
            outtakeMotor.velocity = targetVelocity
        } else {
            outtakeMotor.power = 0.0
        }
        telemetry.addData("Outtake active", active)
        val velocity = outtakeMotor.velocity
        telemetry.addData("Outtake velocity", velocity)
        telemetry.addData("Outtake target velocity", targetVelocity)
        telemetry.addData("Outtake status", if (active && canShoot()) "CAN SHOOT" else "NOT READY")
        if (!debugging) return
        telemetry.addData("Outtake power", outtakeMotor.power)
    }

    fun canShoot(velocity: Double? = null): Boolean {
        return abs(targetVelocity - (velocity ?: outtakeMotor.velocity)) < OuttakeConstants.ALLOWANCE
    }

    fun toggle() {
        active = !active
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

//            return 0.178272 * range.pow(2.0) + 0.0769583 * range + 49.28455
            return 446.88*range.pow(0.295259)

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