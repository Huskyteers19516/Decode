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
    var TURRET_KP: Double = 0.5

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

    @JvmField
    var TURRET_TICKS_PER_REV = 1400.0 / (2 * Math.PI)

    @JvmField
    var BLOCKER_OPEN_POSITION = 0.34

    @JvmField
    var BLOCKER_CLOSED_POSITION = 0.44

    @JvmField
    var HOOD_LOW_ANGLE = 28.0
    @JvmField
    var HOOD_HIGH_ANGLE = 48.0

    @JvmField
    var TRANSFER_ON_POWER = 1.0
    @JvmField
    var TRANSFER_OFF_POWER = 0.0

}