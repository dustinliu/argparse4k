package org.dustinl.argparse4k

import org.dustinl.argparse4k.exception.ArgumentException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
        val detached by parser.flag("-d", "--detached", help = "fdsf")

        val e = assertThrows(ArgumentException::class.java) { version }
        assertTrue(e.message!!.contains("help", ignoreCase = true))
    }

    @Test
    fun `value test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar"))
        val testClass = object {
            val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo")
            val version by parser.flag("-v", help = "help version")
            val detached by parser.flag("-d", "--detached", help = "fdsf")
        }

        assertEquals(false, testClass.version)
        assertEquals(false, testClass.detached)
        assertEquals("bar", testClass.foo)
    }

    @Test
    fun `value missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.foo }
    }

    @Test
    fun `value missing and not required test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo")
        }

        assertEquals(null, testClass.foo)
    }

    @Test
    fun `values test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "ggg"))
        val testClass = object {
            val foo by parser.values("-foo", metavar = "ggg", help = "fo fo fo")
        }

        assertEquals("bar", testClass.foo[0])
        assertEquals("ggg", testClass.foo[1])
    }

    @Test
    fun `values missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.values("-foo", metavar = "ggg", help = "fo fo fo", required = true)
        }

        assertThrows(ArgumentException::class.java) { testClass.foo }
    }

    @Test
    fun `values missing and not required test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val foo by parser.values("-foo", metavar = "ggg", help = "fo fo fo")
        }

        assertEquals(null, testClass.foo)
    }

    @Test
    fun `positional test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "container"))
        val testClass = object {
            val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo")
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
    fun `positional missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positional("container", help = "container name")
        }

        assertEquals(null, testClass.container)
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
    fun `positionals missing test`() {
        val parser = ArgumentParserImpl("testprog", arrayOf())
        val testClass = object {
            val container by parser.positional("container", help = "container name")
        }

        assertEquals(null, testClass.container)
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
}