package org.dustinl.argparse4k

import net.sourceforge.argparse4j.inf.Argument
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

class Delegator<out T> internal constructor(private val parser: ArgumentParser, private val argument: Argument) {
    companion object {
        val logger = LoggerFactory.getLogger(Delegator::class.java)!!
    }

    lateinit var name: String

    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): Delegator<T> {
        name = "$this:${prop.name}"
        logger.debug("bind property '$name'")
        argument.metavar(argument.textualName())
        argument.dest(name)
        return this
    }

    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        logger.debug("get property '$name'")
        return parser.options.get(name)
    }
}
