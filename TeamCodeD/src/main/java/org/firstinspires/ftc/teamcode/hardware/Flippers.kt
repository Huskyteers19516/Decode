package org.firstinspires.ftc.teamcode.hardware

import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.constants.FlippersConstants

class Flippers(hardwareMap: HardwareMap) {
    private val flipperA: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederA")
    private val flipperB: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederB")
    private val flipperC: Servo = hardwareMap.get<Servo>(Servo::class.java, "feederC")

    enum class Flipper {
        A, B, C
    }

    enum class Position {
        UP, DOWN
    }

    var flipperAPosition = Position.DOWN
    var flipperBPosition = Position.DOWN
    var flipperCPosition = Position.DOWN


    init {
        flipperC.direction = Servo.Direction.REVERSE
    }

    fun raiseFlipper(flipper: Flipper) {
        when (flipper) {
            Flipper.A -> flipperAPosition = Position.UP
            Flipper.B -> flipperBPosition = Position.UP
            Flipper.C -> flipperCPosition = Position.UP
        }
        if (listOf(flipperAPosition, flipperBPosition, flipperCPosition).count {
                it == Position.UP
            } > 1) {
            throw RuntimeException("More than one flipper is up")
        }
    }

    fun lowerFlipper(flipper: Flipper) {
        when (flipper) {
            Flipper.A -> flipperAPosition = Position.DOWN
            Flipper.B -> flipperBPosition = Position.DOWN
            Flipper.C -> flipperCPosition = Position.DOWN
        }
    }

    fun periodic() {
        flipperA.position =
            if (flipperAPosition == Position.UP) FlippersConstants.FLIPPER_A_UP_POSITION else FlippersConstants.FLIPPER_A_DOWN_POSITION
        flipperB.position =
            if (flipperBPosition == Position.UP) FlippersConstants.FLIPPER_B_UP_POSITION else FlippersConstants.FLIPPER_B_DOWN_POSITION
        flipperC.position =
            if (flipperCPosition == Position.UP) FlippersConstants.FLIPPER_C_UP_POSITION else FlippersConstants.FLIPPER_C_DOWN_POSITION
    }
}
