package org.akoshterek.backgammon.agent.inputrepresentation;

import org.akoshterek.backgammon.board.Board;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public interface InputRepresentation {
    String getName();

    int getRaceInputsCouns();
    int getCrashedInputsCount();
    int getContactInputsCount();

    double[] calculateRaceInputs(Board anBoard);
    double[] calculateCrashedInputs(Board anBoard);
    double[] calculateContactInputs(Board anBoard);
}
