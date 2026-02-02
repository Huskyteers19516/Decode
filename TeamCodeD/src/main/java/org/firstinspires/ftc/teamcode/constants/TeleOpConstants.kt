package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object TeleOpConstants {
    @JvmField
    var OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR = 100

    @JvmField
    var OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR = 20

    @JvmField
    var TELEOP_BRAKE_MODE = false

    @JvmField
    var DEBUG_MODE = false

    @JvmField
    var COLOR_SENSOR_INTERVAL = 3

    @JvmField
    var FORWARD_MULTIPLIER = 1.0

    @JvmField
    var STRAFE_MULTIPLIER = 1.0

    @JvmField
    var TURN_MULTIPLIER = 0.7
}