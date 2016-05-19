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
class NetworkHolder implements Serializable {
    private static final String DIRECTORY = "bin/";
    private static final long serialVersionUID = -7252112052540032945L;

    NetworkHolder(final BasicNetwork network, final PositionClass networkType) {
        this.network = network;
        this.networkType = networkType;
    }

    private BasicNetwork network;
    private int epoch = 1;
    private PositionClass networkType;
    private TrainingContinuation continuation;

    void serialize(final AgentSettings agentSettings) {
        File file = new File(getResumeFileName(agentSettings, networkType));
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try(OutputStream os = new FileOutputStream(file)) {
            SerializationUtils.serialize(this, os);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void serializeTrainedNetwork(final AgentSettings agentSettings) {
        File file = new File(getTrainedNetworkFileName(agentSettings, networkType));
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        EncogDirectoryPersistence.saveObject(file, network);
    }

    static BasicNetwork deserializeTrainedNetwork(final AgentSettings agentSettings, PositionClass networkType) {
        File file = new File(getTrainedNetworkFileName(agentSettings, networkType));
        if(file.exists()) {
            return (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
        } else {
            return null;
        }
    }

    public static NetworkHolder deserialize(NetworkHolder template, AgentSettings agentSettings) {
        try(InputStream is = new FileInputStream(getResumeFileName(agentSettings, template.networkType))) {
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

    private static String getResumeFileName(AgentSettings agentSettings, PositionClass networkType) {
        return DIRECTORY + agentSettings.agentName() + "-" + PositionClass.getNetworkType(networkType) + "-resume.egs";
    }

    private static String getTrainedNetworkFileName(AgentSettings agentSettings, PositionClass networkType) {
        return DIRECTORY + agentSettings.agentName() + "-" + PositionClass.getNetworkType(networkType) + ".eg";
    }

    public TrainingContinuation getContinuation() {
        return continuation;
    }

    public void setContinuation(TrainingContinuation continuation) {
        this.continuation = continuation;
    }
}
