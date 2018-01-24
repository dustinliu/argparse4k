package org.dustinl.argparse4k.exception

import net.sourceforge.argparse4j.inf.ArgumentParserException
import java.io.PrintWriter
import java.io.StringWriter

open class ArgumentException internal constructor(internal val e: ArgumentParserException)
    : Exception(e.message, e.cause) {
    fun handleErrorMessage(): String = StringWriter().also { e.parser.handleError(e, PrintWriter(it)) }.toString()
    fun help(): String = e.parser.formatHelp()
}
