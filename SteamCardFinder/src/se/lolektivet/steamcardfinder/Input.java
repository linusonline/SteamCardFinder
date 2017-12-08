package se.lolektivet.steamcardfinder;

import org.apache.commons.cli.CommandLine;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Logger;

import static se.lolektivet.steamcardfinder.MyOptions.*;
import static se.lolektivet.steamcardfinder.MyOptions.DEFAULT_INPUT_FILE;
import static se.lolektivet.steamcardfinder.MyOptions.OPTION_FILE_NAME;

public class Input {

   private static final Logger logger = Logger.getLogger(Input.class.getName());

   private static final String INVENTORY_URL = "http://www.steamcardexchange.net/api/request.php?GetInventory";

   private final CommandLine _commandLine;

   Input(CommandLine commandLine) {
      _commandLine = commandLine;
   }

   String getInput() throws IOException {
      if (_commandLine.hasOption(OPTION_READ_FILE)) {
         String fileName = _commandLine.getOptionValue(OPTION_FILE_NAME, DEFAULT_INPUT_FILE);
         logger.info("Reading input from " + fileName + "...");
         return doReadFile(fileName);
      } else {
         logger.info("Connecting to " + INVENTORY_URL + "...");
         String input = doConnectAndGet();
         if (_commandLine.hasOption(OPTION_SAVE)) {
            saveInput(input);
         }
         return input;
      }
   }

   private void saveInput(String input) throws IOException {
      String fileName = _commandLine.getOptionValue(OPTION_FILE_NAME, DEFAULT_INPUT_FILE);
      Path file = Paths.get(fileName);
      Files.write(file, Collections.singletonList(input), Charset.forName("UTF-8"));
   }

   private String doReadFile(String fileName) throws IOException {
      byte[] encoded = Files.readAllBytes(Paths.get(fileName));
      return new String(encoded, "UTF-8");
   }

   private String doConnectAndGet() throws IOException {
      HttpClient httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(INVENTORY_URL);
      HttpResponse response = httpClient.execute(httpGet);
      int responseCode = response.getStatusLine().getStatusCode();
      if (responseCode != 200) {
         throw new RuntimeException("The web server at " + INVENTORY_URL + " returned code " + responseCode);
      }
      InputStream inputStream = response.getEntity().getContent();
      return new Scanner(inputStream,"UTF-8").useDelimiter("\\A").next();
   }

   private void doConnectFail() throws IOException {
      System.setProperty("http.maxRedirects", "50");
      URL url = new URL(INVENTORY_URL);
      HttpURLConnection spoof = (HttpURLConnection)url.openConnection();
      //Spoof the connection so we look like a web browser
//        spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0;    H010818)" );
      spoof.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0" );
      BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
      String strLine = "";
      String finalHTML = "";
      //Loop through every line in the source
      while ((strLine = in.readLine()) != null){
         finalHTML += strLine;
      }
      System.out.println(finalHTML);
   }

}
