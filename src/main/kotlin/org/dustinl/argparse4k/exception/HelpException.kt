package org.dustinl.argparse4k.exception

import net.sourceforge.argparse4j.helper.HelpScreenException

class HelpException(e: HelpScreenException): ArgumentException(e)
