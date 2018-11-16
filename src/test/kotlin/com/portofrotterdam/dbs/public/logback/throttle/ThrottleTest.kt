package com.portofrotterdam.dbs.public.logback.throttle

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.spi.FilterReply
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

class ThrottleTest {
    private val logger = LoggerContext().getLogger("test logger")!!

    @Test fun `when I log ten messages at the same time, only the first should be logged`() {
        val logThrottler = Throttle()
        val message = "This is the same message every time"
        assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.ACCEPT))
        for (i in 2..10) {
            assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.DENY))
        }
    }

    @Test fun `when I log ten messages at intervals greater than secondsToIgnore, all messages should be logged`() {
        var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val logThrottler = Throttle()
        logThrottler.secondsToIgnore = 1
        val message = "This is the same message every time"
        logThrottler.clock = clock
        assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.ACCEPT))
        for (i in 2..10) {
            clock = Clock.fixed(clock.instant().plusSeconds(1), ZoneId.systemDefault())
            logThrottler.clock = clock
            assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.ACCEPT))
        }
    }

    @Test fun `when I log ten messages at intervals smaller than secondsToIgnore, only the first should be logged`() {
        val logThrottler = Throttle()
        var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        logThrottler.clock = clock
        logThrottler.secondsToIgnore = 10
        val message = "This is the same message every time"
        assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.ACCEPT))
        for (i in 2..10) {
            clock = Clock.fixed(clock.instant().plusSeconds(1), ZoneId.systemDefault())
            logThrottler.clock = clock
            assertThat(logThrottler.decide(loggingEvent(message)), `is`(FilterReply.DENY))
        }
    }

    @Test fun `when I log ten different messages, all messages should be logged`() {
        val logThrottler = Throttle()
        for (i in 1..10) {
            assertThat(logThrottler.decide(loggingEvent(UUID.randomUUID().toString())), `is`(FilterReply.ACCEPT))
        }
    }

    @Test fun `when I log more than the max different messages, only the max amount of messages should be logged`() {
        val logThrottler = Throttle()
        logThrottler.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        logThrottler.secondsToIgnore = 10
        logThrottler.maxUniqueMessages = 5
        for (i in 1..5) {
            assertThat(logThrottler.decide(loggingEvent(UUID.randomUUID().toString())), `is`(FilterReply.ACCEPT))
        }
        for (i in 6..10) {
            assertThat(logThrottler.decide(loggingEvent(UUID.randomUUID().toString())), `is`(FilterReply.DENY))
        }
        logThrottler.clock = Clock.fixed(logThrottler.clock.instant().plusSeconds(100), ZoneId.systemDefault())
        for (i in 11..15) {
            assertThat(logThrottler.decide(loggingEvent(UUID.randomUUID().toString())), `is`(FilterReply.ACCEPT))
        }
    }

    private fun loggingEvent(message: String) = LoggingEvent(null, logger, Level.WARN, message, null, arrayOf())
}
