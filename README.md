[![Build Status](https://travis-ci.org/dustinliu/argparse4k.svg?branch=master)](https://travis-ci.org/dustinliu/argparse4k)

# argparse4k

## usage
```kotlin
val parser = ArgumentParserImpl("testprog", arrayOf("-v"))
val version by parser.flag("-v", help = "help version")
val detached by parser.flag("-d", "--detached", help = "fdsf")

assertEquals(true, version)
assertEquals(false, detached)
```

```kotlin
val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar"))
class TestClass {
    val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo")
}

val testClass = TestClass()

assertEquals("bar", testClass.foo)
```
```kotlin
val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "ggg"))
class TestClass  {
    val foo by parser.values("-foo", metavar = "ggg", help = "fo fo fo")
}

val testClass = TestClass()

assertEquals("bar", testClass.foo[0])
assertEquals("ggg", testClass.foo[1])
```

```kotlin
val parser = ArgumentParserImpl("testprog", arrayOf("-foo", "bar", "container"))
class testClass {
    val foo by parser.value("-foo", metavar = "ggg", help = "fo fo fo")
    val container by parser.positional("container", help = "container name")
}

val testClass = TestClass()

assertEquals("bar", testClass.foo)
assertEquals("container", testClass.container)
```

```kotlin
val parser = ArgumentParserImpl("testprog", arrayOf("ccc", "-v"))
val cmdParser = parser.addCommandArgumentParser("ccc")
class TestClass {
    val verbose by parser.flag("-v", help = "container name")
}

class testClass2 {
    val verbose by cmdParser.flag("-v")
}

val testClass = TestClass()
val testClass2 = TestClass2()

assertEquals(false, testClass.verbose)
assertEquals(true, testClass2.verbose)
```
