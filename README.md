logback-throttle
=======

Throttle logback logging to prevent overflow of downstream systems

### Usage
Use it in your logback configuration like here in **logback-spring.xml**:
```xml    
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <include resource="org/springframework/boot/logging/logback/console-appender.xml" />

    <!-- Configure the Sentry appender, overriding the logging threshold to the WARN level -->
    <appender name="SENTRY" class="io.sentry.logback.SentryAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
        <filter class="com.portofrotterdam.dbs.public.logback.throttle.Throttle">
            <secondsToIgnore>600</secondsToIgnore>
            <maxUniqueMessages>100</maxUniqueMessages>
        </filter>
    </appender>

    <root level="INFO">
        <appender-ref ref="SENTRY" />
    </root>
</configuration>
```
### Parameters
- `secondsToIgnore` - The time after receiving a log message to ignore log messages with the same text.
- `maxUniqueMessages` - The maximum amount of unique messages to store. When this amount of unique log messages arrive
        within secondsToIgnore, subsequent logs with a unique message are ignored until secondsToIgnore
        has passed for at least one of the stored messages.
