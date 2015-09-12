package org.akoshterek.backgammon.agent.fa;

import org.akoshterek.backgammon.board.Board;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public interface InputRepresentation {
    double[] calculateRaceInputs(Board anBoard);
    double[] calculateCrashedInputs(Board anBoard);
    double[] calculateContactInputs(Board anBoard);
}
