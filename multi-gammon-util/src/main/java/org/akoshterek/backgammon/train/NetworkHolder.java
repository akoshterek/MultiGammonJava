package org.akoshterek.backgammon.train;

import org.encog.neural.networks.BasicNetwork;

import java.io.Serializable;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class NetworkHolder implements Serializable {
    public BasicNetwork network;
    public int epoch;
}
