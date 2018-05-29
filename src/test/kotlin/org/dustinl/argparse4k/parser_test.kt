package org.dustinl.argparse4k

import net.sourceforge.argparse4j.inf.Argument
import org.dustinl.argparse4k.exception.ArgumentException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.io.PrintWriter
import java.io.StringWriter
import net.sourceforge.argparse4j.inf.ArgumentParser as JavaParser

class ParserTest {
    @Test
    fun `flag test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-v"))
        val version by parser.flag("-v", help = "help version")
        val detached by parser.flag("-d", "--detached", help = "fdsf")

        assertEquals(true, version)
        assertEquals(false, detached)
    }

    @Test
    fun `help test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-h"))
        val version by parser.flag("-v", help = "help version")

        val e = assertThrows(ArgumentException::class.java) { version }
        assertTrue(e.message!!.contains("help", ignoreCase = true))
    }

    @Test
    fun `value test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar"))
        val testClass = object {
            val foo by parser.value("-foo", help = "fo fo fo")
            val version by parser.flag("-v", help = "help version")
            val detached by parser.flag("-d", "--detached", help = "fdsf")
        }

        assertEquals(false, testClass.version)
        assertEquals(false, testClass.detached)
        assertEquals("bar", testClass.foo)
    }

    @Test
    fun `value metavar test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar"))
        val testClass = object {
            val foo by parser.value("-foo", metavar = "foometavar", help = "fo fo fo")
            val version by parser.flag("-v", help = "help version")
            val detached by parser.flag("-d", "--detached", help = "fdsf")
        }

        assertEquals(false, testClass.version)
        assertEquals(false, testClass.detached)
        assertEquals("bar", testClass.foo)
        assertTrue(parser.usage().contains("foometavar"))
    }

    @Test
    fun `value required missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.value("-foo", help = "fo fo fo", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.foo }
    }

    @Test
    fun `value missing and not required test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.value("-foo", help = "fo fo fo")
        }

        assertEquals(null, testClass.foo)
    }

    @Test
    fun `value argument test`() {
        val parser = Mockito.mock(JavaParser::class.java)
        val argument = Mockito.mock(Argument::class.java)
        `when`(parser.addArgument(anyString())).thenReturn(argument)
        `when`(argument.required(anyBoolean())).thenReturn(argument)
        val parserBase = object: ArgumentParserBase(parser) {
            override val options: Options = object: Options {
                override fun <T> get(name: String): T  = listOf<T>()[0]
            }
        }

        parserBase.value("-foo")
        verify(argument, never()).help(anyString())
    }

    @Test
    fun `values test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "ggg"))
        val testClass = object {
            val foo by parser.values("-foo", help = "fo fo fo")
        }

        assertEquals("bar", testClass.foo[0])
        assertEquals("ggg", testClass.foo[1])
    }

    @Test
    fun `values metavar test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "ggg"))
        val testClass = object {
            val foo by parser.values("-foo", help = "fo fo fo", metavar = "foometarvars")
        }

        assertEquals("bar", testClass.foo[0])
        assertEquals("ggg", testClass.foo[1])
        assertTrue(parser.usage().contains("foometarvars"))
    }

    @Test
    fun `values not required missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.values("-foo", help = "fo fo fo")
        }

        assertEquals(null, testClass.foo)
    }

    @Test
    fun `values required missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.values("-foo", help = "fo fo fo", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.foo }
    }

    @Test
    fun `values argument test`() {
        val parser = Mockito.mock(JavaParser::class.java)
        val argument = Mockito.mock(Argument::class.java)
        `when`(parser.addArgument(anyString())).thenReturn(argument)
        `when`(argument.required(anyBoolean())).thenReturn(argument)
        `when`(argument.nargs(anyString())).thenReturn(argument)
        val parserBase = object: ArgumentParserBase(parser) {
            override val options: Options = object: Options {
                override fun <T> get(name: String): T  = listOf<T>()[0]
            }
        }

        parserBase.values("-foo")
        verify(argument, never()).help(anyString())
    }

    @Test
    fun `positional test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "container"))
        val testClass = object {
            val foo by parser.value("-foo", help = "fo fo fo")
            val version by parser.flag("-v", help = "help version")
            val detached by parser.flag("-d", "--detached", help = "fdsf")
            val container by parser.positional("container", help = "container name")
        }

        assertEquals(false, testClass.version)
        assertEquals(false, testClass.detached)
        assertEquals("bar", testClass.foo)
        assertEquals("container", testClass.container)
    }

    @Test
    fun `positional not required missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positional("container", help = "container name")
        }

        assertEquals(null, testClass.container)
    }

    @Test
    fun `positional required missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positional("container", help = "container name", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.container }
    }

    @Test
    fun `positional argument test`() {
        val parser = Mockito.mock(JavaParser::class.java)
        val argument = Mockito.mock(Argument::class.java)
        `when`(parser.addArgument(anyString())).thenReturn(argument)
        `when`(argument.nargs(anyString())).thenReturn(argument)
        val parserBase = object: ArgumentParserBase(parser) {
            override val options: Options = object: Options {
                override fun <T> get(name: String): T  = listOf<T>()[0]
            }
        }

        parserBase.positional("foo")
        verify(argument, never()).help(anyString())
    }

    @Test
    fun `positionals test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("bar", "container"))
        val testClass = object {
            val container by parser.positionals("container", help = "container name")
        }

        assertEquals("bar", testClass.container[0])
        assertEquals("container", testClass.container[1])
    }

    @Test
    fun `positionals not required but missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positionals("container", help = "container name")
        }

        assertEquals(listOf<String>(), testClass.container)
    }

    @Test
    fun `positionals required but missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positionals("container", help = "container name", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.container }
    }

    @Test
    fun `positionals argument test`() {
        val parser = Mockito.mock(JavaParser::class.java)
        val argument = Mockito.mock(Argument::class.java)
        `when`(parser.addArgument(anyString())).thenReturn(argument)
        `when`(argument.nargs(anyString())).thenReturn(argument)
        val parserBase = object: ArgumentParserBase(parser) {
            override val options: Options = object: Options {
                override fun <T> get(name: String): T  = listOf<T>()[0]
            }
        }

        parserBase.positionals("foo")
        verify(argument, never()).help(anyString())
    }

    @Test
    fun `command test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("ccc", "-v"))
        parser.registerCommand("bbb")
        val cmdParser = parser.registerCommand("ccc")
        val testClass = object {
            val verbose by parser.flag("-v", help = "container name")
        }

        val testClass2 = object {
            val verbose by cmdParser.flag("-v")
        }

        assertEquals(false, testClass.verbose)
        assertEquals(true, testClass2.verbose)
        assertEquals("ccc", parser.getCommand())
    }

    @Test
    fun `command unknows test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("bbb", "-v"))
        val cmdParser = parser.registerCommand("ccc")
        val testClass = object {
            val verbose by parser.flag("-v", help = "container name")
        }

        val testClass2 = object {
            val verbose by cmdParser.flag("-v")
        }

        assertThrows(ArgumentException::class.java) { parser.getCommand() }
    }

    @Test
    fun `command missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-v"))
        val cmdParser = parser.registerCommand("ccc")
        val testClass = object {
            val verbose by parser.flag("-v", help = "container name")
        }

        val testClass2 = object {
            val verbose by cmdParser.flag("-v")
        }

        assertThrows(ArgumentException::class.java) { testClass.verbose }
        assertThrows(ArgumentException::class.java) { testClass2.verbose }
    }

    @Test
    fun `description test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        parser.description("xxxxxxxxx")

        assertTrue(parser.usage().contains("xxxxxxxxx"))

    }

    @Test
    fun `epilog test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        parser.epilog("1111111111")

        assertTrue(parser.usage().contains("1111111111"))
    }

    @Test
    fun `print error test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-d"))
        val version by parser.flag("-v", help = "help version")

        val e = assertThrows(ArgumentException::class.java) { version }
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        parser.printError(e, writer)
        assertTrue(stringWriter.toString().contains("-d"))
    }

    @Test
    fun `command parser help test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        parser.registerCommand("COMMAND")
        val subParser: CommandArgumentParser? = parser.getCommandParser("COMMAND")
        assertNotNull(subParser)
        subParser!!.help("xxxxxxxxxx")
        println(parser.usage())
        assertTrue(parser.usage().contains("xxxxxxxxxx"))
    }

    @Test
    fun `exception test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-d"))
        parser.description("xxxxxxxxx")
        val version by parser.flag("-v", help = "help version")

        val e = assertThrows(ArgumentException::class.java) { version }
        assertTrue(e.handleErrorMessage().contains("-d"))
        assertTrue(e.help().contains("xxxxxxxxx"))
    }
}
