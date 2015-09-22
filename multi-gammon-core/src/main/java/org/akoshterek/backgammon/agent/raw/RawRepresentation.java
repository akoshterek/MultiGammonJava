package org.akoshterek.backgammon.agent.raw;

import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.agent.inputrepresentation.PointCodec;
import org.akoshterek.backgammon.board.Board;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class RawRepresentation implements InputRepresentation {
    private final PointCodec codec;

    public RawRepresentation(PointCodec codec) {
        this.codec = codec;
    }

    @Override
    public int getRaceInputsCouns() {
        return getContactInputsCount();
    }

    @Override
    public int getCrashedInputsCount() {
        return getContactInputsCount();
    }

    @Override
    public int getContactInputsCount() {
        return 2 * (codec.getInputsPerPoint() * 24 + 2);
    }

    @Override
    public double[] calculateRaceInputs(Board anBoard) {
        return calculateContactInputs(anBoard);
    }

    @Override
    public double[] calculateCrashedInputs(Board anBoard) {
        return calculateContactInputs(anBoard);
    }

    @Override
    public double[] calculateContactInputs(Board anBoard) {
        double[] inputs = new double[getContactInputsCount()];
        calculateHalfBoard(anBoard.anBoard[0], inputs, 0);
        calculateHalfBoard(anBoard.anBoard[1], inputs, inputs.length / 2);
        return inputs;
    }

    private void calculateHalfBoard(byte[] halfBoard, double[] inputs, int offset) {
        for(int i = 0; i < 24; i++) {
            codec.setPoint(halfBoard[i], inputs, offset + i * codec.getInputsPerPoint());
        }

        inputs[offset + 96] = halfBoard[Board.BAR] / 15.0f;
        int home = Board.TOTAL_MEN;
        for(int i = 0; i < 25; i++)
            home -= halfBoard[i];
        inputs[offset + 97] = home / 15.0f;
    }
}
