package org.dustinl.argparse4k

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Subparser
import org.dustinl.argparse4k.exception.ArgumentException
import org.dustinl.argparse4k.exception.HelpException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import net.sourceforge.argparse4j.inf.ArgumentParser as JavaParser

interface ArgumentParser {
    val options: Options
    fun flag(vararg names: String, help: String? = null): Delegator<Boolean>
    fun value(vararg names: String, help: String? = null, required: Boolean = false, metavar: String? = null)
            : Delegator<String>
    fun values(vararg names: String, help: String? = null, required: Boolean = false, metavar: String? = null)
            : Delegator<List<String>>
    fun positional(name: String, help: String? = null, required: Boolean = false): Delegator<String>
    fun positionals(name: String, help: String? = null, required: Boolean = false): Delegator<List<String>>
    fun description(desc: String)
    fun epilog(epilog: String)
    fun usage(): String
    fun registerCommand(name: String): CommandArgumentParser
    fun getCommandParser(name: String): CommandArgumentParser?
    fun getCommand(): String
    fun printError(e: ArgumentException, writer: PrintWriter = PrintWriter(System.err))
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
        argument.metavar(metavar?:argument.textualName())
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun values(vararg names: String, help: String?, required: Boolean, metavar: String?)
            : Delegator<List<String>> {
        val argument = parser.addArgument(*names).required(required).nargs("*")
        argument.metavar(metavar?:argument.textualName())
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun positional(name: String, help: String?, required: Boolean): Delegator<String> {
        val argument = parser.addArgument(name)
        if (!required) argument.nargs("?")
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun positionals(name: String, help: String?, required: Boolean): Delegator<List<String>> {
        val argument = parser.addArgument(name)
        if (required) argument.nargs("+") else argument.nargs("*")
        help?.run { argument.help(help) }
        return Delegator(this, argument)
    }

    override fun description(desc: String) {
        parser.description(desc)
    }

    override fun epilog(epilog: String) {
        parser.epilog(epilog)
    }

    override fun usage(): String = parser.formatHelp()

    override fun registerCommand(name: String) =
            commandParsers.computeIfAbsent(name) {
                val subParser = parser.addSubparsers().title("subcommands")
                        .description("valid subcommands").metavar("COMMAND")
                        .addParser(name).setDefault("command", name)
                CommandArgumentParser(subParser, this)
            }

    override fun getCommandParser(name: String): CommandArgumentParser? = commandParsers[name]

    override fun getCommand(): String = options.get("command")

    override fun printError(e: ArgumentException, writer: PrintWriter) {
        e.e.parser.handleError(e.e, writer)
    }
}

internal class ArgumentParserImpl constructor(progName: String, private val args: Array<String>)
    : ArgumentParserBase(ArgumentParsers.newFor(progName).build()) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override val options: Options by lazy {
        logger.trace("init options")
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

class CommandArgumentParser internal constructor( internal val subParser: Subparser, rootParser: ArgumentParserBase)
    : ArgumentParserBase(subParser) {
    override val options by lazy { rootParser.options }

    fun help(helpMessage: String) {
        subParser.help(helpMessage)
    }
}

