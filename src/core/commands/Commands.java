package core.commands;

import org.apache.commons.cli.*;

public class Commands {
    //public  CommandLine command;
    public Options options;
    public Commands() {

        Option option_A = Option.builder("m")
                .desc("The command sends a text message to a topic")
                .argName("mBody")
                .hasArg()
                .build();
        Option option_r = Option.builder("t")
                .desc("The t refers to the topic name")
                .argName("tName")
                .hasArg()
                .build();
        Option option_t = Option.builder("l")
                .desc("List all available topics")
                .argName("list")
                .build();


        options = new Options();
        options.addOption(option_A);
        options.addOption(option_r);
        options.addOption(option_t);
    }
    public CommandLine parse(String args[]) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
        return commandLine;


    }
}
