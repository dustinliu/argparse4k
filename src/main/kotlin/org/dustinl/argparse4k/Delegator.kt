package org.dustinl.argparse4k

import net.sourceforge.argparse4j.inf.Argument
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

class Delegator<T>(private val parser: ArgumentParser, private val argument: Argument) {
    val logger = LoggerFactory.getLogger(Delegator::class.java)

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): Delegator<T> {
        logger.debug("bind property '${composeName(thisRef, prop)}'")
        argument.dest(composeName(thisRef, prop))
        return this
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): T {
        logger.debug("get property '${composeName(thisRef, prop)}'")
        return parser.options.get(composeName(thisRef, prop))
    }

    private fun composeName(thisRef: Any, prop: KProperty<*>) = "${thisRef::class}:${prop.name}"
}
