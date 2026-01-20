package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.utils.Slot

class Flippers(hardwareMap: HardwareMap) {
    private var flippers = mapOf<Slot, Servo>(
        Slot.A to hardwareMap.get<Servo>(Servo::class.java, "feederA"),
        Slot.B to hardwareMap.get<Servo>(Servo::class.java, "feederB"),
        Slot.C to hardwareMap.get<Servo>(Servo::class.java, "feederC"),
    )

    enum class Position {
        UP, DOWN
    }

    var flipperPositions = mutableMapOf(
        Slot.A to Position.DOWN,
        Slot.B to Position.DOWN,
        Slot.C to Position.DOWN,
    )


    init {
        flippers[Slot.C]!!.direction = Servo.Direction.REVERSE
    }

    fun raiseFlipper(slot: Slot) {
        if (flipperPositions.filter { (flipperSlot, _) ->
                flipperSlot != slot
            }.count { (_, position) ->
                position == Position.UP
            } > 0) {
            throw RuntimeException("More than one flipper is up")
        }
        flipperPositions[slot] = Position.UP
    }

    fun lowerFlipper(slot: Slot) {
        flipperPositions[slot] = Position.DOWN
    }

    fun periodic(telemetry: TelemetryManager, debugging: Boolean = false) {
        for ((slot, position) in flipperPositions) {
            val servo = flippers[slot] ?: continue
            when (slot) {
                Slot.A -> {
                    servo.position = when (position) {
                        Position.UP -> FlippersConstants.FLIPPER_A_UP_POSITION
                        Position.DOWN -> FlippersConstants.FLIPPER_A_DOWN_POSITION
                    }
                }

                Slot.B -> {
                    servo.position = when (position) {
                        Position.UP -> FlippersConstants.FLIPPER_B_UP_POSITION
                        Position.DOWN -> FlippersConstants.FLIPPER_B_DOWN_POSITION
                    }
                }

                Slot.C -> {
                    servo.position = when (position) {
                        Position.UP -> FlippersConstants.FLIPPER_C_UP_POSITION
                        Position.DOWN -> FlippersConstants.FLIPPER_C_DOWN_POSITION
                    }
                }
            }
        }

        flipperPositions.forEach { (slot, position) ->
            telemetry.addData("$slot flipper position", position)
        }

        if (!debugging) return

        flippers.forEach { (slot, servo) -> telemetry.addData("$slot flipper position", servo.position) }
    }
}
