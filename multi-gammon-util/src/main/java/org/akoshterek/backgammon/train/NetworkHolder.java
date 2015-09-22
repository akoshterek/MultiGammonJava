package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.board.PositionClass;
import org.apache.commons.lang3.SerializationUtils;
import org.encog.neural.networks.BasicNetwork;

import java.io.*;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class NetworkHolder implements Serializable {
    //@SuppressWarnings("unused")
    //public NetworkHolder() {}
    public NetworkHolder(BasicNetwork network, PositionClass networkType, AgentSettings agentSettings) {
        this.network = network;
        this.networkType = networkType;
        this.agentSettings = agentSettings;
    }

    private BasicNetwork network;
    private int epoch = 1;
    private PositionClass networkType;
    private AgentSettings agentSettings;

    public void serialize(String agentName) {
        try(OutputStream os = new FileOutputStream(getResumeFileName(agentName))) {
            //EncogDirectoryPersistence
            SerializationUtils.serialize(this, os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static NetworkHolder deserialize(NetworkHolder template) {
        try(InputStream is = new FileInputStream(template.agentSettings.getAgentName())) {
            return SerializationUtils.<NetworkHolder>deserialize(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BasicNetwork getNetwork() {
        return network;
    }

    public int getEpoch() {
        return epoch;
    }

    public void incEpoch() {
        epoch++;
    }

    private String getResumeFileName(String agentName) {
        return "bin/" + agentName + PositionClass.getNetworkType(networkType) + "-resume.egs";
    }
}
