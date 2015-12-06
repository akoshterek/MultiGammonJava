package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.move.AuchKey;

/**
 * @author Alex
 *         date: 05.12.2015
 */
public class ETraceEntry {
    public double[] input;
    public PositionClass pc = PositionClass.CLASS_CONTACT;
    public AuchKey auch;
    public double eTrace = 1.0;
}
