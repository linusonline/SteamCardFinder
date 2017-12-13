package se.lolektivet.steamcardfinder;

import java.util.logging.*;

/**
 * Created by Linus on 2017-03-24.
 */
public class MyConsoleHandler extends StreamHandler {
   public MyConsoleHandler(Level level) {
      setOutputStream(System.out);
      setFormatter(LoggingConf.SIMPLER_FORMATTER);
      setLevel(level);
   }

   /**
    * Publish a <tt>LogRecord</tt>.
    * <p>
    * The logging request was made initially to a <tt>Logger</tt> object,
    * which initialized the <tt>LogRecord</tt> and forwarded it here.
    * <p>
    * @param  record  description of the log event. A null record is
    *                 silently ignored and is not published
    */
   @Override
   public void publish(LogRecord record) {
      super.publish(record);
      flush();
   }

   /**
    * Override <tt>StreamHandler.close</tt> to do a flush but not
    * to close the output stream.  That is, we do <b>not</b>
    * close <tt>System.err</tt>.
    */
   @Override
   public void close() {
      flush();
   }
}
