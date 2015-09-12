package org.akoshterek.backgammon.util;

import org.apache.commons.cli.*;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class OptionsBuilder {
    private static final String HELP_OPTION = "help";
    private static final String WARRANTY_OPTION = "warranty";
    private static final String LICENSE_OPTION = "copying";
    private static final String VERBOSE_OPTION = "verbose";

    private static final String AGENT_OPTION = "agent";
    private static final String BENCH_AGENT_OPTION = "bench-agent";
    private static final String DEFAULT_BENCH_AGENT = "Heuristic";
    private static final String TRAIN_GAMES_OPTION = "train-games";
    private static final String BENCH_GAMES_OPTION = "bench-games";
    private static final String BENCH_PERIOD_OPTION = "bench-period";


    private static final Options options = new Options();
    private static CommandLine commandLine = null;

    public static void build() {
        options.addOption("h", HELP_OPTION, false, "produce help message");
        options.addOption("c", LICENSE_OPTION, false, "show license");
        options.addOption("w", WARRANTY_OPTION, false, "show warranty");
        options.addOption("v", VERBOSE_OPTION, false, "show game log");
        options.addOption(Option.builder("A").longOpt(AGENT_OPTION).argName("agent").desc("agent(s) to train").hasArg().build());
        options.addOption(Option.builder("B").longOpt(BENCH_AGENT_OPTION).argName("benchmark agent").desc("benchmark agent").hasArg().build());
        options.addOption(Option.builder("T").longOpt(TRAIN_GAMES_OPTION).argName("training games").desc("number of games for training").hasArg().type(Number.class).build());
        options.addOption(Option.builder("G").longOpt(BENCH_GAMES_OPTION).argName("benchmark games").desc("number of games for benchmark").hasArg().type(Number.class).build());
        options.addOption(Option.builder("P").longOpt(BENCH_PERIOD_OPTION).argName("benchmark period").desc("benchmark every n games").hasArg().type(Number.class).build());
    }

    public static OptionsBean parse(String[] args) {
        final CommandLineParser parser = new DefaultParser();
        OptionsBean bean = new OptionsBean();
        // parse the command line arguments
        try {
            commandLine = parser.parse(options, args);

            bean.setIsHelp(commandLine.hasOption(HELP_OPTION));
            bean.setIsWarranty(commandLine.hasOption(WARRANTY_OPTION));
            bean.setIsLicense(commandLine.hasOption(LICENSE_OPTION));
            bean.setIsVerbose(commandLine.hasOption(VERBOSE_OPTION));
            bean.setAgentNames(commandLine.getOptionValues(AGENT_OPTION));
            bean.setBenchmarkAgentName(commandLine.getOptionValue(BENCH_AGENT_OPTION, DEFAULT_BENCH_AGENT));
            bean.setTrainingGames(getIntOption(TRAIN_GAMES_OPTION, 10000));
            bean.setBenchmarkGames(getIntOption(BENCH_GAMES_OPTION, 1000));
            bean.setBenchmarkPeriod(getIntOption(BENCH_PERIOD_OPTION, 10000));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return bean;
    }

    public static void printHelp(String cmdLineSyntax) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(cmdLineSyntax, options, true );
    }

    private static int getIntOption(String optionName, int defaultValue) {
        Number number;
        try {
            number = (Number)commandLine.getParsedOptionValue(optionName);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return number == null ? defaultValue : number.intValue();
    }
}

