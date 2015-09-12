package org.akoshterek.backgammon.eval;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public interface Gammons {
    /* gammon possible by side on roll */
    int G_POSSIBLE = 0x1;
    /* backgammon possible by side on roll */
    int BG_POSSIBLE = 0x2;

    /* gammon possible by side not on roll */
    int OG_POSSIBLE = 0x4;

    /* backgammon possible by side not on roll */
    int OBG_POSSIBLE = 0x8;
}
