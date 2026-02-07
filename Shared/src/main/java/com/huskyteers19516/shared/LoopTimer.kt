package com.huskyteers19516.shared

import com.bylazar.telemetry.TelemetryManager
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource.Monotonic.markNow
import kotlin.time.measureTime

class LoopTimer {
    private val sectionTotalTimes = mutableMapOf<String, Duration>()

    private var loops = 0
    private var lastStartTime: TimeMark? = null
    private var lastEndTime: TimeMark? = null
    private var totalLoopTime = Duration.ZERO
    private var totalIdleTime = Duration.ZERO

    fun start() {
        lastStartTime = markNow()
    }

    @OptIn(ExperimentalContracts::class)
    fun section(sectionName: String, k: () -> Unit) {
        contract {
            callsInPlace(k, InvocationKind.EXACTLY_ONCE)
        }

        val time = measureTime(k)
        sectionTotalTimes[sectionName] = time + (sectionTotalTimes[sectionName] ?: Duration.ZERO)
    }

    fun end(telemetry: TelemetryManager) {
        loops++
        telemetry.addData("Loops", loops)

        var measuredTotal = Duration.ZERO
        sectionTotalTimes.forEach { (section, time) ->
            measuredTotal += time
            telemetry.addData("$section average loop time", time / loops)
        }
        telemetry.addData("Average measured total loop time", measuredTotal / loops)

        val actualThisLoop = lastStartTime?.elapsedNow() ?: Duration.ZERO
        totalLoopTime += actualThisLoop
        telemetry.addData("Average actual total loop time", totalLoopTime / loops)

        telemetry.addData("Average unmeasured loop time", (totalLoopTime - measuredTotal) / loops)

        if (lastEndTime != null && lastStartTime != null) {
            val idleThisLoop = lastEndTime!!.elapsedNow() - lastStartTime!!.elapsedNow()
            if (idleThisLoop > Duration.ZERO) {
                totalIdleTime += idleThisLoop
            }
            telemetry.addData("Average idle time", totalIdleTime / (loops - 1))
        }

        lastEndTime = markNow()
    }
}
