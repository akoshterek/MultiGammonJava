package org.akoshterek.backgammon.dispatch;

import org.akoshterek.backgammon.License;
import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.AgentFactory;
import org.akoshterek.backgammon.agent.Agent;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.util.OptionsBean;
import org.akoshterek.backgammon.util.OptionsBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Alex
 *         date 18.07.2015.
 */
public class Dispatcher {
    private OptionsBean options;

    public Dispatcher() {
        OptionsBuilder.build();
    }

    public boolean init(String[] args) {
        License.printBanner();
        options = OptionsBuilder.parse(args);

        if(options.isHelp()) {
            OptionsBuilder.printHelp("multigammon");
            return false;
        }
        else if(options.isLicense()) {
            License.printLicense();
            return false;
        }
        else if(options.isWarranty()) {
            License.printWarranty();
            return false;
        }

        Path currentPath = Paths.get("").toAbsolutePath().normalize();
        Evaluator.getInstance().setSeed(16000000L);
        Evaluator.getInstance().load(currentPath);
        return true;
    }

    public void run() {
        String[] agentNames = options.getAgentNames();
        String benchAgenName = options.getBenchmarkAgentName();

        long start = System.currentTimeMillis();
        Arrays.stream(agentNames).parallel().forEach(agentName ->
                runAgentIteration(agentName,
                    benchAgenName,
                    options.getTrainingGames(),
                    options.getBenchmarkGames(),
                    options.getBenchmarkPeriod())
        );
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time " + formatTime(end - start));
    }

    private void runAgentIteration(String agentName, String benchAgentName,
                                   int trainingGames, int benchmarkGames, int benchmarkPeriod) {

        Agent agent1 = AgentFactory.createAgent(agentName);
        Agent benchAgent = AgentFactory.createAgent(benchAgentName);
        assert(agent1 != null && benchAgent != null);
        Agent agent2;
        if(agent1 instanceof Cloneable) {
            agent2 = ((AbsAgent)agent1).clone();
        }
        else {
            agent2 = AgentFactory.createAgent(agentName);
        }

        runIteration(agent1, benchAgent, agent2, trainingGames, benchmarkGames, benchmarkPeriod);
    }

    private void runIteration(Agent agent1, Agent benchAgent, Agent agent2,
                                    int trainGames, int benchmarkGames, int benchmarkPeriod) {
        GameDispatcher gameDispatcher = new GameDispatcher(agent1, agent2);
        gameDispatcher.setIsShowLog(options.isVerbose());

        if (trainGames > 0) {
            for (int game = 0; game < trainGames; game += benchmarkPeriod) {
                //training
                gameDispatcher.playGames(Math.min(benchmarkPeriod, trainGames), true);
                agent1.save();

                //benchmark
                GameDispatcher benchDispatcher = new GameDispatcher(agent1, benchAgent);
                benchDispatcher.setIsShowLog(options.isVerbose());
                benchDispatcher.playGames(benchmarkGames, false);
                benchDispatcher.printStatistics();
            }
        } else {
            //benchmark
            GameDispatcher benchDispatcher = new GameDispatcher(agent1, benchAgent);
            benchDispatcher.setIsShowLog(options.isVerbose());
            benchDispatcher.playGames(benchmarkGames, false);
            benchDispatcher.printStatistics();
        }
    }

    private static String formatTime(long millis) {
        return DurationFormatUtils.formatDuration(millis, "HH:mm:ss");
    }
}
