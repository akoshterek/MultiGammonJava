package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.RepresentationFactory;
import org.akoshterek.backgammon.agent.inputrepresentation.SuttonCodec;
import org.akoshterek.backgammon.agent.raw.RawRepresentation;
import org.akoshterek.backgammon.board.PositionClass;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class AgentTrainer {
    public static void main(String[] args) {
        String representationName = args[0];
        String hiddenNeuronsCount = args[1];

        AgentSettings settings = new AgentSettings();
        settings.representation = RepresentationFactory.createInputRepresentation(representationName);
        settings.hiddenNeuronCount = Integer.parseInt(hiddenNeuronsCount);

        NetworkTrainer trainer = new NetworkTrainer(settings, PositionClass.CLASS_CONTACT);
        trainer.trainNetwork();
    }
}
