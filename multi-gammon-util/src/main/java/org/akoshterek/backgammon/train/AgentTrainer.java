package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.RepresentationFactory;
import org.akoshterek.backgammon.board.PositionClass;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class AgentTrainer {
    public static void main(String[] args) {
        String agentName = args[0];

        AgentSettings settings = new AgentSettings();
        settings.representation = RepresentationFactory.createInputRepresentation(agentName);
        settings.agentName = agentName;
        settings.hiddenNeuronCount = getHiddenNeuronsCount(agentName);

        trainNetwork(settings, PositionClass.CLASS_CONTACT);
        trainNetwork(settings, PositionClass.CLASS_CRASHED);
        trainNetwork(settings, PositionClass.CLASS_RACE);
    }

    private static void trainNetwork(AgentSettings settings, PositionClass networkType) {
        NetworkTrainer trainer = new NetworkTrainer(settings, networkType);
        trainer.trainNetwork();
    }

    private static int getHiddenNeuronsCount(String agentName) {
        String[] tokens = agentName.split("-");
        return Integer.parseInt(tokens[2]);
    }
}
