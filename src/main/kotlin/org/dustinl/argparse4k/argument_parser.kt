package org.dustinl.argparse4k

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.dustinl.argparse4k.exception.ArgumentException
import org.slf4j.LoggerFactory
import net.sourceforge.argparse4j.inf.ArgumentParser as JavaParser

interface ArgumentParser {
    val options: Options
    fun flag(vararg names: String, help: String? = null): Delegator<Boolean>
    fun value(vararg names: String, help: String? = null, required: Boolean = false, metavar: String? = null)
            : Delegator<String>
    fun values(vararg names: String, help: String? = null, required: Boolean = false, metavar: String? = null)
            : Delegator<List<String>>
    fun positional(name: String, help: String? = null): Delegator<String>
    fun positionals(name: String, help: String? = null): Delegator<List<String>>
    fun description(desc: String)
    fun epilog(epilog: String)
    fun help(): String
    fun addCommandArgumentParser(name: String): CommandArgumentParser
}

interface Options {
    fun <T> get(name: String): T
}

object ArgumentParserFactory {
    fun createParser(progName: String, args: Array<String>) = ArgumentParserImpl(progName, args)
}

abstract class ArgumentParserBase(val parser: JavaParser): ArgumentParser {
    override fun flag(vararg names: String, help: String?): Delegator<Boolean> {
        val argument = parser.addArgument(*names).action(Arguments.storeTrue()).setDefault(false)
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun value(vararg names: String, help: String?, required: Boolean, metavar: String?)
            : Delegator<String> {
        val argument = parser.addArgument(*names).required(required)
        metavar?.also { argument.metavar(metavar) }
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun values(vararg names: String, help: String?, required: Boolean, metavar: String?)
            : Delegator<List<String>> {
        val argument = parser.addArgument(*names).required(required).nargs("*")
        metavar?.also { argument.metavar(metavar) }
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun positional(name: String, help: String?): Delegator<String> {
        val argument = parser.addArgument(name).nargs("?")
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun positionals(name: String, help: String?): Delegator<List<String>> {
        val argument = parser.addArgument(name).nargs("*")
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun description(desc: String) {
        parser.description(desc)
    }

    override fun epilog(epilog: String) {
        parser.epilog(epilog)
    }

    override fun help(): String = parser.formatHelp()

    override fun addCommandArgumentParser(name: String) = CommandArgumentParser(name, this)
}

class ArgumentParserImpl(progName: String, private val args: Array<String>)
    : ArgumentParserBase(ArgumentParsers.newFor(progName).build()) {
    val logger = LoggerFactory.getLogger(this::class.java)

    override val options: Options by lazy {
        logger.debug("init options")
        try {
            val namespace = parser.parseArgs(args)
            object: Options {
                override fun <T> get(name: String): T = namespace.get<T>(name)
            }
        } catch(e: ArgumentParserException) {
            throw ArgumentException(e)
        }
    }

}

class CommandArgumentParser(name: String, rootParser: ArgumentParserBase)
    : ArgumentParserBase(rootParser.parser.addSubparsers().addParser(name)) {
    override val options by lazy { rootParser.options }
}

