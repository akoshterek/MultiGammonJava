package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.SuttonCodec;
import org.akoshterek.backgammon.agent.raw.RawRepresentation;
import org.akoshterek.backgammon.board.PositionClass;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class AgentTrainer {
    public static void main(String[] args) {
        AgentSettings settings = new AgentSettings();
        settings.pointCodec = new SuttonCodec();
        settings.representation = new RawRepresentation(settings.pointCodec);
        settings.hiddenNeuronCount = 40;
        NetworkTrainer trainer = new NetworkTrainer(settings, PositionClass.CLASS_CONTACT);
        trainer.trainNetwork();
    }
}
