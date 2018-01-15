import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.helpers.NOPAppender

import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.TRACE

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
    pattern = "[%thread] %F:%L - %msg%n"
    }
}

appender("NULL", NOPAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "[%thread] %F:%L - %msg%n"
    }
}

if (System.getenv("CI") != "true") {
    root(ERROR, ["STDOUT"])
} else {
    root(ERROR, ["NULL"])
}

logger("org.dustinl.argparse4k", TRACE)
