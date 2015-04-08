package fr.ribesg.blob.util

import org.apache.log4j.Logger
import org.apache.log4j.Priority
import java.util.HashMap

/**
 * @author Ribesg
 */

public object Log {

    // Backup Logger
    private val log: Logger = Logger.getLogger("Blob")

    /* Log filtering */

    private val filters: MutableMap<String, String> = HashMap()

    public fun addFilter(regex: String, replacement: String) {
        this.filters[regex] = replacement
    }

    private fun filter(message: String): String {
        var result = message
        for ((regex, replacement) in filters) {
            result = result.replaceAll(regex, replacement)
        }
        return result
    }

    /* Logging */

    public fun isEnabledFor(level: Priority): Boolean = this.log.isEnabledFor(level)

    public fun warn(message: String) {
        this.log.warn(filter(message));
    }

    public fun warn(message: String, t: Throwable) {
        this.log.warn(filter(message), t);
    }

    public fun log(callerFqcn: String, level: Priority, message: String, t: Throwable) {
        this.log.log(callerFqcn, level, filter(message), t);
    }

    public fun log(level: Priority, message: String) {
        this.log.log(level, filter(message));
    }

    public fun log(level: Priority, message: String, t: Throwable) {
        this.log.log(level, filter(message), t);
    }

    public fun info(message: String, t: Throwable) {
        this.log.info(filter(message), t);
    }

    public fun info(message: String) {
        this.log.info(filter(message));
    }

    public fun fatal(message: String) {
        this.log.fatal(filter(message));
    }

    public fun fatal(message: String, t: Throwable) {
        this.log.fatal(filter(message), t);
    }

    public fun error(message: String, t: Throwable) {
        this.log.error(filter(message), t);
    }

    public fun error(message: String) {
        this.log.error(filter(message));
    }

    public fun debug(message: String, t: Throwable) {
        this.log.debug(filter(message), t);
    }

    public fun debug(message: String) {
        this.log.debug(filter(message));
    }
}
