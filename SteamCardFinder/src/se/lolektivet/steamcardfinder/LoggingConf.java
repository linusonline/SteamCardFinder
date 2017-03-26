package se.lolektivet.steamcardfinder;

import java.io.*;
import java.util.Properties;
import java.util.logging.*;

/**
 * Created by Linus on 2017-03-24.
 */
public class LoggingConf {
   private static Logger _lolektivetLogger;
   private static Handler _fileHandler;
//

   //   public LoggingConf() {
//      System.out.println("LoggingConf()");
//      try {
//         Properties properties = new Properties();
//         properties.setProperty("se.lolektivet.handlers", "se.lolektivet.steamcardfinder.MyConsoleHandler");
//         properties.setProperty("se.lolektivet.useParentHandlers", "false");
//         properties.setProperty("se.lolektivet.level", "ALL");
//
//         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//         properties.store(byteArrayOutputStream, "");
//         final PipedOutputStream out = new PipedOutputStream();
//         PipedInputStream in = new PipedInputStream(out);
//         byteArrayOutputStream.writeTo(out);
//         out.close();
//
//         Properties outProps = new Properties();
//         outProps.load(in);
//         System.out.println(outProps);
//
//         LogManager.getLogManager().readConfiguration(in);
//      } catch (IOException e) {
//         e.printStackTrace();
//      }
//   }
   private static boolean _loggingToFile;

   public static void init(boolean logToFile, Level logLevel) {
      _lolektivetLogger = Logger.getLogger("se.lolektivet");

      _lolektivetLogger.setUseParentHandlers(false);
      _lolektivetLogger.addHandler(new MyConsoleHandler(logLevel));
      _lolektivetLogger.setLevel(logLevel);

      _loggingToFile = logToFile;
      if (_loggingToFile) {
         setFileHandler();
      }
   }

   private static void setFileHandler() {
      if (_fileHandler == null) {
         try {
            _fileHandler = new FileHandler("log/linuswars-log.%u.%g.txt", true);
            _fileHandler.setFormatter(SIMPLER_FORMATTER);
         } catch (IOException e) {
            _lolektivetLogger.log(Level.WARNING, "Could not create file handler for logging!", e);
         }
      }
      _lolektivetLogger.addHandler(_fileHandler);
   }

   private static void unsetFileHandler() {
      _lolektivetLogger.removeHandler(_fileHandler);
   }

   static final Formatter SIMPLER_FORMATTER = new Formatter() {
      @Override
      public String format(LogRecord record) {
         // TODO: Proper date format.
         return (record.getLevel().equals(Level.INFO) ? "" : record.getLevel() + ": ") + record.getMessage() + "\n" + getStacktrace(record);
      }
   };

   private static String getStacktrace(LogRecord record) {
      String stacktrace = "";
      Throwable t = record.getThrown();
      if (t != null) {
         StringWriter sw = new StringWriter();
         t.printStackTrace(new PrintWriter(sw));
         stacktrace = sw.toString();
      }
      return stacktrace;
   }
}
