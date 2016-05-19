package org.akoshterek.backgammon

import org.akoshterek.backgammon.dispatch.Dispatcher

object MultiGammon {
  def main(args: Array[String]) {
    val dispatcher: Dispatcher = new Dispatcher
    if (dispatcher.init(args)) {
      dispatcher.run()
    }
  }
}