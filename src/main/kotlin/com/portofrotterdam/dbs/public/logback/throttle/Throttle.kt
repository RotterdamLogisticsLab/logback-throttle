package com.portofrotterdam.dbs.public.logback.throttle

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.spi.FilterReply.ACCEPT
import ch.qos.logback.core.spi.FilterReply.DENY
import java.time.Clock

class Throttle : Filter<ILoggingEvent>() {
    var secondsToIgnore: Long = 60 * 5
    var clock = Clock.systemDefaultZone()!!
    var maxUniqueMessages: Int = 50

    private val millisecondsToIgnore get() = (1000L * secondsToIgnore)
    private val previousLogTimes = LinkedHashMap<String, Long>()

    override fun decide(event: ILoggingEvent): FilterReply {
        removeExpired()
        return when {
            previousLogTimes.size >= maxUniqueMessages -> DENY
            previousLogTimes[event.message] == null -> {
                previousLogTimes[event.message] = clock.millis()
                ACCEPT
            }
            else -> DENY
        }
    }

    private fun removeExpired() {
        val threshold = clock.millis() - millisecondsToIgnore
        previousLogTimes.entries.takeWhile { it.value <= threshold }.forEach { previousLogTimes.remove(it.key) }
    }
}
