package org.akoshterek.backgammon;

/**
 * @author Alex
 *         date 18.07.2015.
 */
public class MultiGammon {
    public static void main(String[] args) throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        if(dispatcher.init(args)) {
            dispatcher.run();
        }
    }
}
