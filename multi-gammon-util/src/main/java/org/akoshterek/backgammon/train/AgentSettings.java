package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.agent.inputrepresentation.PointCodec;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class AgentSettings {
    public InputRepresentation representation;
    public PointCodec pointCodec;
    public int hiddenNeuronCount;
}
