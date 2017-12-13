package se.lolektivet.steamcardfinder;

import org.apache.commons.cli.*;

public class MyOptions {
   static final String OPTION_READ_FILE = "f";
   private static final String OPTION_READ_FILE_LONG = "file";
   static final String OPTION_FILE_NAME = "n";
   private static final String OPTION_FILE_NAME_LONG = "filename";
   static final String OPTION_GAME_LIMIT = "l";
   private static final String OPTION_GAME_LIMIT_LONG = "limit";
   static final String OPTION_HELP = "h";
   private static final String OPTION_HELP_LONG = "help";
   static final String OPTION_VERBOSE = "v";
   private static final String OPTION_VERBOSE_LONG = "verbose";
   static final String OPTION_SAVE = "s";
   private static final String OPTION_SAVE_LONG = "save";
   static final String OPTION_MY_CARDS = "c";
   private static final String OPTION_MY_CARDS_LONG = "cards";
   static final String OPTION_MY_CARDS_FILE = "d";
   private static final String OPTION_MY_CARDS_FILE_LONG = "cardsfile";
   static final String OPTION_MY_CREDITS = "r";
   private static final String OPTION_MY_CREDITS_LONG = "credits";
   static final String OPTION_MY_WANTEDS = "w";
   private static final String OPTION_MY_WANTEDS_LONG = "wanted";

   static final String DEFAULT_INPUT_FILE = "input.json";
   static final String DEFAULT_MY_CARDS_FILE = "mycards.txt";
   static final String DEFAULT_EXCLUDED_CARDS_FILE = "excluded.txt";
   static final String DEFAULT_WANTED_CARDS_FILE = "wanted.txt";

   private Options _options;

   private void createOptions() {
      Option help = Option.builder(OPTION_HELP).longOpt(OPTION_HELP_LONG)
            .desc("Print this help message").build();

      Option readFile = Option.builder(OPTION_READ_FILE).longOpt(OPTION_READ_FILE_LONG)
            .desc("Read web page from input file instead of from web.").build();

      Option filename = Option.builder(OPTION_FILE_NAME).longOpt(OPTION_FILE_NAME_LONG)
            .hasArg().argName("file")
            .desc("Name of the file to read/write web page from/to. Default is " + DEFAULT_INPUT_FILE).build();

      Option gameLimit = Option.builder(OPTION_GAME_LIMIT).longOpt(OPTION_GAME_LIMIT_LONG)
            .hasArg().argName("nr")
            .desc("Max number of games to list.").build();

      Option verbose = Option.builder(OPTION_VERBOSE).longOpt(OPTION_VERBOSE_LONG)
            .desc("Print verbose information.").build();

      Option save = Option.builder(OPTION_SAVE).longOpt(OPTION_SAVE_LONG)
            .desc("When reading from web, save webpage to file specified by --" + filename.getLongOpt() + " option. Default is " + DEFAULT_INPUT_FILE).build();

      Option myCards = Option.builder(OPTION_MY_CARDS).longOpt(OPTION_MY_CARDS_LONG)
            .desc("Read and summarize list of owned game cards. Each line of the list file should be on the form " +
                  "'<card-amount>:<drop-amount>:<game-name>'. Game name must match the name in the online inventory exactly. Default " +
                  "file to read from is " + DEFAULT_MY_CARDS_FILE).build();

      Option myCardsFile = Option.builder(OPTION_MY_CARDS_FILE).longOpt(OPTION_MY_CARDS_FILE_LONG)
            .hasArg().argName("file")
            .desc("Name of the file to read owned game card list from. Default is " + DEFAULT_MY_CARDS_FILE).build();

      Option myCredits = Option.builder(OPTION_MY_CREDITS).longOpt(OPTION_MY_CREDITS_LONG)
            .hasArg().argName("credit-amount")
            .desc("Number of credits you already have in your Steam Card Exchange account.").build();

      Option myWanted = Option.builder(OPTION_MY_WANTEDS).longOpt(OPTION_MY_WANTEDS_LONG)
            .desc("Read wanted card sets from " + DEFAULT_WANTED_CARDS_FILE + " and print useful info about them. Each " +
                  "line of the file should have a game name exactly matching the name in the online inventory.").build();

      _options = new Options();

      _options.addOption(help);
      _options.addOption(filename);
      _options.addOption(readFile);
      _options.addOption(gameLimit);
      _options.addOption(verbose);
      _options.addOption(save);
      _options.addOption(myCards);
      _options.addOption(myCardsFile);
      _options.addOption(myCredits);
      _options.addOption(myWanted);
   }

   CommandLine parseArgs(String[] args) throws ParseException {
      createOptions();
      CommandLineParser parser = new DefaultParser();
      return parser.parse(_options, args);
   }

   void printHelp() {
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp("SteamCardFinder", _options, true);
   }

}
