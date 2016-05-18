package org.akoshterek.backgammon;

import org.akoshterek.backgammon.dispatch.Dispatcher;

/**
 * @author Alex
 *         date 18.07.2015.
 */
public class MultiGammon {
    public static void main(final String[] args) {
        Dispatcher dispatcher = new Dispatcher();
        if(dispatcher.init(args)) {
            dispatcher.run();
        }
    }
}
