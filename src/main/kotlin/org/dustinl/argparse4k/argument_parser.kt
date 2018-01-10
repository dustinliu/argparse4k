package org.dustinl.argparse4k

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.dustinl.argparse4k.exception.ArgumentException
import org.dustinl.argparse4k.exception.HelpException
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
    fun registerCommand(name: String): CommandArgumentParser
    fun getCommandParser(name: String): CommandArgumentParser
    fun getCommand(): String
    fun printError(e: ArgumentException)
}

interface Options {
    fun <T> get(name: String): T
}

object ArgumentParserFactory {
    fun createParser(progName: String, args: Array<String>): ArgumentParser = ArgumentParserImpl(progName, args)
}

abstract class ArgumentParserBase(val parser: JavaParser): ArgumentParser {
    private val commandParsers = mutableMapOf<String, CommandArgumentParser>()

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

    override fun registerCommand(name: String) =
            commandParsers.computeIfAbsent(name) { CommandArgumentParser(name, this) }

    override fun getCommandParser(name: String) = registerCommand(name)

    override fun getCommand(): String = options.get("command")

    override fun printError(e: ArgumentException) {
        e.e.parser.handleError(e.e)
    }
}

internal class ArgumentParserImpl constructor(progName: String, private val args: Array<String>)
    : ArgumentParserBase(ArgumentParsers.newFor(progName).build()) {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    override val options: Options by lazy {
        logger.debug("init options")
        try {
            val namespace = parser.parseArgs(args)
            object : Options {
                override fun <T> get(name: String): T = namespace.get<T>(name)
            }
        } catch (e: HelpScreenException) {
            throw HelpException(e)
        } catch (e: ArgumentParserException) {
            throw ArgumentException(e)
        }
    }
}

class CommandArgumentParser internal constructor(val name: String, rootParser: ArgumentParserBase)
    : ArgumentParserBase(rootParser.parser.addSubparsers().addParser(name).setDefault("command", name)) {
    override val options by lazy { rootParser.options }
}

