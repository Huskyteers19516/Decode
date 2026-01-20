package org.firstinspires.ftc.teamcode.constants

import com.bylazar.configurables.annotations.Configurable

@Configurable
object DriveConstants {
    @JvmField
    var TURN_COEFFICIENT = 0.01

    @JvmField
    var DEFAULT_DRIVE_MODE_IS_ROBOT_CENTRIC = true

    @JvmField
    var NORMAL_MODE_SPEED = 1.0

    @JvmField
    var SLOW_MODE_SPEED = 0.5
}