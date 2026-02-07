package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object ColorSensorConstants {
    @JvmField
    var MINIMUM_ALPHA = 220

    @JvmField
    var PURPLE_SCORE_MULTIPLIER = 1.3

    @JvmField
    var SHOOT_EVEN_IF_NOT_DESIRED_COLOR = true

    @JvmField
    var SHOOT_EVEN_IF_UNKNOWN_COLOR = true
}