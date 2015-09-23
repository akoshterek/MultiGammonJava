package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.board.PositionClass;
import org.apache.commons.lang3.SerializationUtils;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.TrainingContinuation;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.*;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class NetworkHolder implements Serializable {
    private static final String DIRECTORY = "bin/";

    public NetworkHolder(BasicNetwork network, PositionClass networkType) {
        this.network = network;
        this.networkType = networkType;
    }

    private BasicNetwork network;
    private int epoch = 1;
    private PositionClass networkType;
    private TrainingContinuation continuation;

    public void serialize(AgentSettings agentSettings) {
        File file = new File(getResumeFileName(agentSettings));
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try(OutputStream os = new FileOutputStream(file)) {
            SerializationUtils.serialize(this, os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void serializeTrainedNetwork(AgentSettings agentSettings) {
        File file = new File(getTrainedNetworkFileName(agentSettings));
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        EncogDirectoryPersistence.saveObject(file, network);
    }

    public static NetworkHolder deserialize(NetworkHolder template, AgentSettings agentSettings) {
        try(InputStream is = new FileInputStream(template.getResumeFileName(agentSettings))) {
            return SerializationUtils.<NetworkHolder>deserialize(is);
        } catch (FileNotFoundException e) {
            return null;
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

    private String getResumeFileName(AgentSettings agentSettings) {
        return DIRECTORY + agentSettings.agentName + "-" + PositionClass.getNetworkType(networkType) + "-resume.egs";
    }

    private String getTrainedNetworkFileName(AgentSettings agentSettings) {
        return DIRECTORY + agentSettings.agentName + "-" + PositionClass.getNetworkType(networkType) + "-resume.eg";
    }

    public TrainingContinuation getContinuation() {
        return continuation;
    }

    public void setContinuation(TrainingContinuation continuation) {
        this.continuation = continuation;
    }
}
