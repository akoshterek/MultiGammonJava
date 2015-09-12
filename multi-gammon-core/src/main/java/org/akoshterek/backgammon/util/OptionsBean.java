package org.akoshterek.backgammon.util;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class OptionsBean {
    private boolean isHelp;
    private boolean isWarranty;
    private boolean isLicense;
    private String[] agentNames;
    private String benchmarkAgentName;
    private int trainingGames;
    private int benchmarkGames;
    private int benchmarkPeriod;
    private boolean isVerbose;

    public boolean isHelp() {
        return isHelp;
    }

    public void setIsHelp(boolean isHelp) {
        this.isHelp = isHelp;
    }

    public boolean isWarranty() {
        return isWarranty;
    }

    public void setIsWarranty(boolean isWarranty) {
        this.isWarranty = isWarranty;
    }

    public boolean isLicense() {
        return isLicense;
    }

    public void setIsLicense(boolean isLicense) {
        this.isLicense = isLicense;
    }

    public String[] getAgentNames() {
        return agentNames;
    }

    public void setAgentNames(String[] agentNames) {
        this.agentNames = agentNames;
    }

    public String getBenchmarkAgentName() {
        return benchmarkAgentName;
    }

    public void setBenchmarkAgentName(String benchmarkAgentName) {
        this.benchmarkAgentName = benchmarkAgentName;
    }

    public int getTrainingGames() {
        return trainingGames;
    }

    public void setTrainingGames(int trainingGames) {
        this.trainingGames = trainingGames;
    }

    public int getBenchmarkGames() {
        return benchmarkGames;
    }

    public void setBenchmarkGames(int benchmarkGames) {
        this.benchmarkGames = benchmarkGames;
    }

    public int getBenchmarkPeriod() {
        return benchmarkPeriod;
    }

    public void setBenchmarkPeriod(int benchmarkPeriod) {
        this.benchmarkPeriod = benchmarkPeriod;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public void setIsVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }
}
