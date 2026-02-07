package org.firstinspires.ftc.teamcode.hardware

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.exec
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.sequence
import dev.frozenmilk.dairy.mercurial.continuations.Continuations.wait
import org.firstinspires.ftc.teamcode.constants.FeedersConstants
import org.firstinspires.ftc.teamcode.constants.FeedersConstants.FULL_SPEED
import org.firstinspires.ftc.teamcode.constants.FeedersConstants.STOP_SPEED

class Feeders(hardwareMap: HardwareMap) {
    val leftFeeder: CRServo = hardwareMap.get(CRServo::class.java, "left_feeder")
    val rightFeeder: CRServo = hardwareMap.get(CRServo::class.java, "right_feeder")


    init {
        leftFeeder.direction = DcMotorSimple.Direction.FORWARD
        rightFeeder.direction = DcMotorSimple.Direction.REVERSE

        leftFeeder.power = STOP_SPEED
        rightFeeder.power = STOP_SPEED
    }

    fun feed() {
        leftFeeder.power = FULL_SPEED
        rightFeeder.power = FULL_SPEED

    }

    fun stop() {
        leftFeeder.power = STOP_SPEED
        rightFeeder.power = STOP_SPEED
    }

    fun shoot() = sequence(
        exec {
            feed()
        },
        wait(FeedersConstants.FEED_TIME_SECONDS),
        exec {
            stop()
        }
    )
}