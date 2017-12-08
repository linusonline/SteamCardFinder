package se.lolektivet.steamcardfinder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.logging.Level;

import static se.lolektivet.steamcardfinder.MyOptions.OPTION_HELP;
import static se.lolektivet.steamcardfinder.MyOptions.OPTION_VERBOSE;

/**
 * Created by Linus on 2017-03-23.
 */
public class Main {
   private static final int VERSION_MAJOR = 0;
   private static final int VERSION_MINOR = 1;
   private static final int VERSION_REVISION = 1;
   private static final String VERSION_STRING = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_REVISION;

   private final MyOptions _options = new MyOptions();
   private CommandLine _commandLine;

   public static void main(String[] args) {
      new Main().tryDoAll(args);
   }

   private void tryDoAll(String[] args) {
      try {
         doAll(args);
      } catch (ParseException | IOException e) {
         e.printStackTrace();
      }
   }

   private void doAll(String[] args) throws ParseException, IOException {
      _commandLine = _options.parseArgs(args);
      initLogging();
      printVersionInfo();
      run();
   }

   private void initLogging() {
      Level level = _commandLine.hasOption(OPTION_VERBOSE) ? Level.INFO : Level.WARNING;
      LoggingConf.init(false, level);
   }

   private void printVersionInfo() {
      Analysis.stdoutln("SteamCardFinder v" + VERSION_STRING);
   }

   private void run() throws IOException {
      if (_commandLine.hasOption(OPTION_HELP)) {
         _options.printHelp();
         return;
      }
      new Analysis(_commandLine).analyze();
   }

}
