package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object OuttakeConstants {
    @JvmField
    var SHOOTER_KP: Double = 300.0

    @JvmField
    var SHOOTER_KI: Double = 0.0

    @JvmField
    var SHOOTER_KD: Double = 0.0

    @JvmField
    var SHOOTER_KS: Double = 10.0

    @JvmField
    var TURRET_KP: Double = 300.0

    @JvmField
    var TURRET_KI: Double = 0.0

    @JvmField
    var TURRET_KD: Double = 0.0

    @JvmField
    var TURRET_KS: Double = 10.0

    @JvmField
    var DEFAULT_TARGET_VELOCITY: Double = 1450.0

    @JvmField
    var ALLOWANCE: Double = 60.0
}