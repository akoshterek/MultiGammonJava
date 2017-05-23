package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.move.AuchKey;

import java.util.Arrays;

/**
 * @author Alex
 *         date: 05.12.2015
 */
public class ETraceEntry {
    public ETraceEntry(float[] input, PositionClass pc, AuchKey auch) {
        this.input = Arrays.copyOf(input, input.length);
        this.pc = pc;
        this.auch = new AuchKey(auch);
        eTrace = 1.0f;
    }

    public float[] input;
    public PositionClass pc;
    public AuchKey auch;
    public float eTrace;
}
