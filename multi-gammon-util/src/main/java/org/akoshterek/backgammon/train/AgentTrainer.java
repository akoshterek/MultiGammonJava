package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.RepresentationFactory;
import org.akoshterek.backgammon.board.PositionClass;
import org.encog.Encog;

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

        trainContact(settings);
        trainCrashed(settings);
        trainRace(settings);
        Encog.getInstance().shutdown();
    }

    private static void trainContact(AgentSettings settings) {
        System.out.println("Started contact network training");
        trainNetwork(settings, PositionClass.CLASS_CONTACT);
        System.out.println("Finished contact network training");
    }

    private static void trainCrashed(AgentSettings settings) {
        System.out.println("Started crashed network training");
        trainNetwork(settings, PositionClass.CLASS_CRASHED);
        System.out.println("Finished crashed network training");
    }

    private static void trainRace(AgentSettings settings) {
        System.out.println("Started race network training");
        trainNetwork(settings, PositionClass.CLASS_RACE);
        System.out.println("Finished race network training");
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
