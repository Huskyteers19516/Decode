package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor
import com.qualcomm.robotcore.hardware.ColorRangeSensor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.constants.ColorSensorConstants
import com.huskyteers19516.shared.Slot

class ColorSensors(hardwareMap: HardwareMap) {
    val colorSensorA1: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorA1")
    val colorSensorA2: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorA2")
    val colorSensorB1: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorB1")
    val colorSensorB2: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorB2")
    val colorSensorC1: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorC1")
    val colorSensorC2: LynxI2cColorRangeSensor = hardwareMap.get(LynxI2cColorRangeSensor::class.java, "colorSensorC2")

    var slots = mutableMapOf(
        Slot.A to Artifact.UNKNOWN,
        Slot.B to Artifact.UNKNOWN,
        Slot.C to Artifact.UNKNOWN,
    )

    fun update() {
        slots = slots.mapValues { (slot, _) -> getSlotArtifact(slot) }.toMutableMap()
    }

    fun telemetry(telemetry: TelemetryManager) {
        slots.forEach { (slot, color) -> telemetry.addData("$slot slot", color) }
    }

    fun getBestSlot(desiredColor: Artifact): Slot? {
        update()
        return slots.toList().firstOrNull { (_, color) ->
            println(color)
            desiredColor == color
        }?.first ?: slots.toList().firstOrNull { (_, color) ->
            ColorSensorConstants.SHOOT_EVEN_IF_NOT_DESIRED_COLOR && color in listOf(Artifact.GREEN, Artifact.PURPLE)
        }?.first ?: slots.toList().firstOrNull { (_, color) ->
            ColorSensorConstants.SHOOT_EVEN_IF_UNKNOWN_COLOR && color == Artifact.UNKNOWN
        }?.first
    }

    fun debugTelemetry(telemetry: TelemetryManager) {
        mapOf(
            "A" to identifyArtifact(colorSensorA1, colorSensorA2),
            "B" to identifyArtifact(colorSensorB1, colorSensorB2),
            "C" to identifyArtifact(colorSensorC1, colorSensorC2)
        ).forEach { (slot, color) -> telemetry.addData("$slot slot", color) }

        val nameMap = mapOf(
            "A1" to colorSensorA1,
            "A2" to colorSensorA2,
            "B1" to colorSensorB1,
            "B2" to colorSensorB2,
            "C1" to colorSensorC1,
            "C2" to colorSensorC2
        )
        nameMap.forEach { (name, sensor) ->
            telemetry.addData(
                "$name Color",
                identifyColor(sensor)
            )
        }
        nameMap.forEach { (name, sensor) -> debugTelemetry(sensor, name, telemetry) }
    }

    fun getSlotArtifact(slot: Slot): Artifact {
        return when (slot) {
            Slot.A -> identifyArtifact(
                colorSensorA1,
                colorSensorA2
            )

            Slot.B -> identifyArtifact(
                colorSensorB1,
                colorSensorB2
            )

            Slot.C -> identifyArtifact(
                colorSensorC1,
                colorSensorC2
            )
        }
    }

    companion object {
        enum class Artifact {
            GREEN, PURPLE, UNKNOWN, NONE
        }

        fun identifyArtifact(colorSensor1: ColorRangeSensor, colorSensor2: ColorRangeSensor): Artifact {
            val artifact1 = identifyColor(colorSensor1)
            val artifact2 = identifyColor(colorSensor2)

            return if (artifact1 == artifact2) {
                artifact1
            } else if (listOf(artifact1, artifact2).count { it != Artifact.NONE } == 1) {
                if (artifact1 != Artifact.NONE) artifact1 else artifact2
            } else {
                Artifact.UNKNOWN
            }
        }

        fun identifyColor(colorSensor: ColorRangeSensor): Artifact {
            return if (colorSensor.alpha() < ColorSensorConstants.MINIMUM_ALPHA) {
                Artifact.NONE
            } else if (colorSensor.green() > ColorSensorConstants.PURPLE_SCORE_MULTIPLIER * (colorSensor.red() + colorSensor.blue()) / 2) {
                Artifact.GREEN
            } else {
                Artifact.PURPLE
            }
        }

        fun debugTelemetry(colorSensor: ColorRangeSensor, name: String, telemetry: TelemetryManager) {
            telemetry.addData("$name Red", colorSensor.red())
            telemetry.addData("$name Green", colorSensor.green())
            telemetry.addData("$name Blue", colorSensor.blue())
            telemetry.addData(
                "$name Purple Score",
                ColorSensorConstants.PURPLE_SCORE_MULTIPLIER * (colorSensor.blue() + colorSensor.red()) / 2
            )
            telemetry.addData("$name Alpha", colorSensor.alpha())
        }
    }
}