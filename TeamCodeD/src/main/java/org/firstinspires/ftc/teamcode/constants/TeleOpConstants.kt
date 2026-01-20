package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object TeleOpConstants {
    @JvmField
    var OUTTAKE_TARGET_VELOCITY_BIG_ADJUSTMENT_FACTOR = 100

    @JvmField
    var OUTTAKE_TARGET_VELOCITY_SMALL_ADJUSTMENT_FACTOR = 20

    @JvmField
    var TELEOP_BRAKE_MODE = true

    @JvmField
    var DEBUG_MODE = false

    @JvmField
    var COLOR_SENSOR_INTERVAL = 3

}