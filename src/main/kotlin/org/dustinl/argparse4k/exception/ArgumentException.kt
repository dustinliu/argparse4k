package org.dustinl.argparse4k.exception

import net.sourceforge.argparse4j.inf.ArgumentParserException
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.Exception

open class ArgumentException(internal val e: ArgumentParserException) : Exception(e.message, e.cause) {
    fun help(): String = StringWriter().also { e.parser.handleError(e, PrintWriter(it)) }.toString()
}

