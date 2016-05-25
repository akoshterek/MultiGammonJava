package org.akoshterek.backgammon

import org.akoshterek.backgammon.dispatch.Dispatcher

object MultiGammon extends App{
    val dispatcher: Dispatcher = new Dispatcher
    if (dispatcher.init(args)) {
        dispatcher.run()
    }
}