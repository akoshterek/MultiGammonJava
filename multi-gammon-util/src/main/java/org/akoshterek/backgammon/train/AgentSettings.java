package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;

import java.io.Serializable;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class AgentSettings implements Serializable {
    public InputRepresentation representation;
    public int hiddenNeuronCount;
    public String getAgentName() {
        return representation.getName() + "-" + hiddenNeuronCount;
    }
}
