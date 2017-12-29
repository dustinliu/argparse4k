package org.dustinl.argparse4k.exception

import net.sourceforge.argparse4j.inf.ArgumentParserException
import kotlin.Exception

class ArgumentException(e: ArgumentParserException) : Exception(e.message, e.cause)
