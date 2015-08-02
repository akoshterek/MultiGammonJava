package org.akoshterek.backgammon;

import org.apache.commons.cli.*;


/**
 * @author Alex
 *         date 18.07.2015.
 */
public class Dispatcher {

    private final Options options = new Options();
    private int trainingGames = 0;
    private int benchmarkGames = 0;
    private int benchmarkPeriod = 0;

    public Dispatcher() {
        options.addOption("h", "help", false, "produce help message");
        options.addOption("c", "copying", false, "show license");
        options.addOption("w", "warranty", false, "show warranty");
        options.addOption(Option.builder("A").longOpt("agent").argName("agent").desc("agent(s) to train").hasArg().build());
        options.addOption(Option.builder("B").longOpt("bench-agent").argName("benchmark agent").desc("benchmark agent").hasArg().build());
        options.addOption(Option.builder("T").longOpt("train-games").argName("training games").desc("number of games for training").hasArg().build());
        options.addOption(Option.builder("G").longOpt("bench-games").argName("benchmark games").desc("number of games for benchmark").hasArg().build());
        options.addOption(Option.builder("P").longOpt("bench-period").argName("benchmark period").desc("benchmark every n games").hasArg().build());
    }

    public boolean init(String[] args) throws ParseException {
        banner();

        try {
            final CommandLineParser parser = new DefaultParser();
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if(line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("multigammon", options, true );
                return false;
            }
            else if(line.hasOption("c")) {
                printTextArray(License.copying);
                return false;
            }
            else if(line.hasOption("w")) {
                printTextArray(License.warranty);
                return false;
            }


        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            throw exp;
        }


        return true;
    }

    public void run() {

    }

    //private:
    //int m_argc;
    //char **m_argv;

    /*
    po::options_description m_desc;
    po::variables_map m_vm;
    void runAgentIteration(const char *agentName, const char *benchAgentName);
    void runIteration(BgAgent *agent1, BgAgent *benchAgent, BgAgent *agent2,
                      int trainGames, int benchmarkGames, int benchmarkPeriod);
    */

    private static void banner() {
        printTextArray(License.banner);
    }

    private static void printTextArray(String[] text) {
        for(String s : text) {
            System.out.print(s);
        }
    }
}
