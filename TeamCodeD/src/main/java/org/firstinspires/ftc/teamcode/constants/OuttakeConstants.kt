package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object OuttakeConstants {
    @JvmField
    var KP: Double = 300.0

    @JvmField
    var KI: Double = 0.0

    @JvmField
    var KD: Double = 0.0

    @JvmField
    var KS: Double = 0.0

    @JvmField
    var DEFAULT_TARGET_VELOCITY: Double = 1500.0

    @JvmField
    var ALLOWANCE: Double = 50.0
}