package org.akoshterek.backgammon.eval;

/**
 * @author Alex
 *         date 25.07.2015.
 */
public class EvalContext {
    /**
     * cubeful evaluation
     */
    public boolean isCubeful = false;
    public int plies = 0;
    public boolean usePrune = false;
    public boolean isDetermenistic = false;
    /**
     * standard deviation
     */
    public float noise = 0;
}
