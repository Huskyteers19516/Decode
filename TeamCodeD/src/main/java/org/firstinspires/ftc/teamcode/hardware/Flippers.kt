package org.firstinspires.ftc.teamcode.hardware

import com.bylazar.telemetry.TelemetryManager
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.constants.FlippersConstants
import org.firstinspires.ftc.teamcode.utils.Slot

class Flippers(hardwareMap: HardwareMap) {
    private val flipperA: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederA")
    private val flipperB: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederB")
    private val flipperC: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederC")

    enum class Position {
        UP, DOWN
    }

    var flipperAPosition = Position.DOWN
    var flipperBPosition = Position.DOWN
    var flipperCPosition = Position.DOWN


    init {
        flipperC.direction = Servo.Direction.REVERSE
    }

    fun raiseFlipper(slot: Slot) {
        when (slot) {
            Slot.A -> flipperAPosition = Position.UP
            Slot.B -> flipperBPosition = Position.UP
            Slot.C -> flipperCPosition = Position.UP
        }
        if (listOf(flipperAPosition, flipperBPosition, flipperCPosition).count {
                it == Position.UP
            } > 1) {
            throw RuntimeException("More than one flipper is up")
        }
    }

    fun lowerFlipper(slot: Slot) {
        when (slot) {
            Slot.A -> flipperAPosition = Position.DOWN
            Slot.B -> flipperBPosition = Position.DOWN
            Slot.C -> flipperCPosition = Position.DOWN
        }
    }

    fun periodic(telemetry: TelemetryManager, debugging: Boolean = false) {
        flipperA.position =
            if (flipperAPosition == Position.UP) FlippersConstants.FLIPPER_A_UP_POSITION else FlippersConstants.FLIPPER_A_DOWN_POSITION
        flipperB.position =
            if (flipperBPosition == Position.UP) FlippersConstants.FLIPPER_B_UP_POSITION else FlippersConstants.FLIPPER_B_DOWN_POSITION
        flipperC.position =
            if (flipperCPosition == Position.UP) FlippersConstants.FLIPPER_C_UP_POSITION else FlippersConstants.FLIPPER_C_DOWN_POSITION

        telemetry.addData("Flipper A state", flipperAPosition)
        telemetry.addData("Flipper B state", flipperBPosition)
        telemetry.addData("Flipper C state", flipperCPosition)
        if (!debugging) return
        telemetry.addData("Flipper A position", flipperA.position)
        telemetry.addData("Flipper B position", flipperB.position)
        telemetry.addData("Flipper C position", flipperC.position)
    }
}
