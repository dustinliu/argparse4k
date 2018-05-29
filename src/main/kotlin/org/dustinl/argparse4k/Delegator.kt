package org.dustinl.argparse4k

import net.sourceforge.argparse4j.inf.Argument
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

class Delegator<out T> internal constructor(private val parser: ArgumentParser, private val argument: Argument) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Delegator::class.java)
    }

    private lateinit var name: String

    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): Delegator<T> {
        name = "$this:${prop.name}"
        logger.trace("bind property '$name'")
        argument.dest(name)
        return this
    }

    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        logger.trace("get property '$name'")
        return parser.options.get(name)
    }
}
