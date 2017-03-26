package se.lolektivet.steamcardfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Linus on 2017-03-23.
 */
public class Main {
   private static final String OPTION_READ_FILE = "f";
   private static final String OPTION_READ_FILE_LONG = "file";
   private static final String OPTION_FILE_NAME = "n";
   private static final String OPTION_FILE_NAME_LONG = "filename";
   private static final String OPTION_GAME_LIMIT = "l";
   private static final String OPTION_GAME_LIMIT_LONG = "limit";
   private static final String OPTION_HELP = "h";
   private static final String OPTION_HELP_LONG = "help";
   private static final String OPTION_VERBOSE = "v";
   private static final String OPTION_VERBOSE_LONG = "verbose";
   private static final String OPTION_SAVE = "s";
   private static final String OPTION_SAVE_LONG = "save";
   private static final String OPTION_MY_CARDS = "c";
   private static final String OPTION_MY_CARDS_LONG = "cards";
   private static final String OPTION_MY_CARDS_FILE = "d";
   private static final String OPTION_MY_CARDS_FILE_LONG = "cardsfile";

   private static final String DEFAULT_INPUT_FILE = "input.html";
   private static final String DEFAULT_MY_CARDS_FILE = "mycards.txt";
   private static final String DEFAULT_EXCLUDED_CARDS_FILE = "excluded.txt";

   private static final String INVENTORY_URL = "http://www.steamcardexchange.net/index.php?inventory";

   private static final String PROP_GAME_NAME = "name";
   private static final String PROP_GAME_ID = "id";
   private static final String PROP_GAME_CARDPRICE = "cardPrice";
   private static final String PROP_GAME_CARDS_IN_SET = "cardsInSeries";
   private static final String PROP_GAME_CARDS_IN_STOCK = "cardsInStock";
   private static final String PROP_GAME_UNIQUE_IN_STOCK = "uniqueInStock";
   private static final String PROP_GAME_SETS_IN_STOCK = "setsInStock";
   private static final String PROP_GAME_MAX_CARD_STOCK = "maxCardStock";
   private static final String PROP_GAME_SET_PRICE = "setPrice";
   private static final String PROP_GAME_MY_AMOUNT = "myAmount";
   private static final String PROP_GAME_MY_DROPS = "myDrops";

   private static Logger logger = Logger.getLogger(Main.class.getName());

   private final List<JsonObject> allGames = new ArrayList<>();

   private CommandLine _commandLine;
   private Options _options;

   public static void main(String[] args) {
      new Main().tryDoAll(args);
   }

   private void tryDoAll(String[] args) {
      try {
         doAll(args);
      } catch (ParseException | IOException | SAXException | ParserConfigurationException e) {
         e.printStackTrace();
      }
   }

   private void doAll(String[] args) throws ParseException, ParserConfigurationException, SAXException, IOException {
      createOptions();
      parseArgs(args);
      initLogging();
      run();
   }

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
                  "'<card-amount>:<game-name>'. Game name must match the name in the online inventory exactly. Default " +
                  "file to read from is " + DEFAULT_MY_CARDS_FILE).build();

      Option myCardsFile = Option.builder(OPTION_MY_CARDS_FILE).longOpt(OPTION_MY_CARDS_FILE_LONG)
            .hasArg().argName("file")
            .desc("Name of the file to read owned game card list from. Default is " + DEFAULT_MY_CARDS_FILE).build();

      _options = new Options();

      _options.addOption(help);
      _options.addOption(filename);
      _options.addOption(readFile);
      _options.addOption(gameLimit);
      _options.addOption(verbose);
      _options.addOption(save);
      _options.addOption(myCards);
      _options.addOption(myCardsFile);
   }

   private void parseArgs(String[] args) throws ParseException {
      CommandLineParser parser = new DefaultParser();
      _commandLine = parser.parse(_options, args);
   }

   private void initLogging() {
      Level level = _commandLine.hasOption(OPTION_VERBOSE) ? Level.ALL : Level.INFO;
      LoggingConf.init(false, level);
   }

   private void run() throws IOException, ParserConfigurationException, SAXException {
      if (_commandLine.hasOption(OPTION_HELP)) {
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp("SteamCardFinder", _options, true);
      } else {
         String fullInput;
         if (_commandLine.hasOption(OPTION_READ_FILE)) {
            String fileName = _commandLine.getOptionValue(OPTION_FILE_NAME, DEFAULT_INPUT_FILE);
            logger.config("Reading input from " + fileName + "...");
            fullInput = doReadFile(fileName);
         } else {
            logger.config("Connecting to " + INVENTORY_URL + "...");
            fullInput = doConnectAndGet();
            if (_commandLine.hasOption(OPTION_SAVE)) {
               saveInput(fullInput);
            }
         }
         doParseByHand(fullInput);
         if (_commandLine.hasOption(OPTION_MY_CARDS)) {
            doAnalyzeMyCards(_commandLine.getOptionValue(OPTION_MY_CARDS_FILE, DEFAULT_MY_CARDS_FILE));
         }
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

   private void doParseByHand(String fullContent) {
      allGames.clear();

      logger.config("Parsing input page...");
      int gamePricesStart = fullContent.indexOf("var gameprices=") + 15;
      int gamePricesEnd = fullContent.indexOf(";", gamePricesStart);
      String gamePricesString = fullContent.substring(gamePricesStart, gamePricesEnd);

      int stockListStart = fullContent.indexOf("var stocklist=") + 14;
      int stockListEnd = fullContent.indexOf(";", stockListStart);
      String stockListString = fullContent.substring(stockListStart, stockListEnd);

      int nameListStart = fullContent.indexOf("></option>") + 12;
      int nameListEnd = fullContent.indexOf("</select>", nameListStart);
      String nameList = fullContent.substring(nameListStart, nameListEnd);

      JsonParser jsonParser = new JsonParser();
      JsonObject gamePricesJson = jsonParser.parse(gamePricesString).getAsJsonObject();

      JsonObject stockListJson = jsonParser.parse(stockListString).getAsJsonObject();

      logger.config(gamePricesJson.size() + " games with card price.");
      logger.config(stockListJson.size() + " games with stock info.");
      logger.config("Analyzing...");
      for (Map.Entry<String, JsonElement> entry : gamePricesJson.entrySet()) {
         JsonObject game = new JsonObject();
         String id = entry.getKey();
         game.addProperty(PROP_GAME_ID, id);
         game.addProperty(PROP_GAME_CARDPRICE, entry.getValue().getAsString());

         JsonElement stockInfoElement = stockListJson.get(id);
         if (stockInfoElement != null) {
            JsonArray stockInfo = stockInfoElement.getAsJsonArray();

            int cardsInSeries = stockInfo.get(0).getAsInt();
            int uniqueInStock = stockInfo.get(2).getAsInt();
            game.addProperty(PROP_GAME_CARDS_IN_SET, cardsInSeries);
            game.addProperty(PROP_GAME_CARDS_IN_STOCK, stockInfo.get(1).getAsString());
            game.addProperty(PROP_GAME_UNIQUE_IN_STOCK, uniqueInStock);
            game.addProperty(PROP_GAME_SETS_IN_STOCK, uniqueInStock < cardsInSeries ? 0 : stockInfo.get(3).getAsInt());
            game.addProperty(PROP_GAME_MAX_CARD_STOCK, stockInfo.get(4).getAsString());

            game.addProperty(PROP_GAME_SET_PRICE, entry.getValue().getAsInt() * stockInfo.get(0).getAsInt());

            int gameNameStart = nameList.indexOf(">", nameList.indexOf("inventorygame-appid-" + id)) + 1;
            String gameName = nameList.substring(gameNameStart, nameList.indexOf("<", gameNameStart));
            if (!gameName.isEmpty()) {
               game.addProperty(PROP_GAME_NAME, gameName);
               allGames.add(game);
            }
         }
      }

      List<JsonObject> sortedRelevantGames = allGames.stream()
            .filter(this::moreThanOneSet)
            .sorted(this::compareGames)
            .collect(Collectors.toList());

      try {
         FileInputStream fis = new FileInputStream(DEFAULT_EXCLUDED_CARDS_FILE);
         BufferedReader excludesFile = new BufferedReader(new InputStreamReader(fis));
         List<String> excludedGames = excludesFile.lines().collect(Collectors.toList());
         sortedRelevantGames = sortedRelevantGames.stream()
               .filter(game -> excludedGames.stream().noneMatch(gameName -> gameName.equals(getGameName(game))))
               .collect(Collectors.toList());

      } catch (FileNotFoundException e) {
         logger.config("Could not open excludes file " + DEFAULT_EXCLUDED_CARDS_FILE + ". [" + e.getLocalizedMessage() + "]");
      }


      logger.info("");
      logger.info("---------------------- SteamCardExchange Inventory Summary ---------------------");
      logger.info("Total number of games: " + allGames.size());

      logger.info("Number of games with more than one full set: " + sortedRelevantGames.size());
      Stream<JsonObject> finalList = sortedRelevantGames.stream();
      if (_commandLine.hasOption(OPTION_GAME_LIMIT)) {
         int max = Integer.parseInt(_commandLine.getOptionValue(OPTION_GAME_LIMIT));
         logger.info("Limiting list to " + max + " cheapest games.");
         finalList = finalList.limit(max);
      }
      logger.info("");
      logger.info("------------------------- Cheapest available Card Sets -------------------------");
      logger.info("");
      logger.info("[Setsize / Sets / Cost - Name]");
      finalList.forEach(this::printResult);
   }

   private int compareGames(JsonObject game1, JsonObject game2) {
      return Integer.compare(getSetPrice(game1), getSetPrice(game2));
   }

   private boolean moreThanOneSet(JsonObject jsonObject) {
      return jsonObject.get(PROP_GAME_SETS_IN_STOCK).getAsInt() > 1;
   }

   private void printResult(JsonObject game) {
      logger.info(game.get(PROP_GAME_CARDS_IN_SET) + " / " + game.get(PROP_GAME_SETS_IN_STOCK) + " / " + getSetPrice(game) + " - " + getGameName(game));
   }

   private void doAnalyzeMyCards(String fileName) throws FileNotFoundException {
      FileInputStream fis = new FileInputStream(fileName);
      BufferedReader myCardsFile = new BufferedReader(new InputStreamReader(fis));

      List<JsonObject> myGamesWithInfo = myCardsFile.lines()
            .map(this::lineToGame)
            .filter(Objects::nonNull)
            .flatMap(myGame -> allGames.stream()
                  .filter(game -> gamesEqual(myGame, game))
                  .map(game -> mergeGames(game, myGame)))
            .collect(Collectors.toList());

      int ownedWorth = myGamesWithInfo.stream().mapToInt(game -> (getMyAmount(game) * getCardPrice(game))).sum();
      int dropsWorth = myGamesWithInfo.stream().mapToInt(game -> (getMyDrops(game) * getCardPrice(game))).sum();
      int totalWorth = ownedWorth + dropsWorth;

      // TODO: Warn about possibly overstocked cards. Will not be precise based on the info we have,
      // but good enough to warn when "maxCardStock" is 8.
      //logger.info("Sets you own that have overstocked cards:");

      logger.info("");
      logger.info("----------------------------- My Game Card Summary -----------------------------");
      logger.info("[(Set) Worth * Cards = Total - Name]");
      logger.info("");
      if (totalWorth > 0) {
         if (ownedWorth > 0) {
            logger.info("--OWNED CARDS: " + ownedWorth);
            myGamesWithInfo.forEach(this::printMyOwned);
         } else {
            logger.info("--NO OWNED CARDS");
         }
         logger.info("");
         if (dropsWorth > 0) {
            logger.info("--REMAINING DROPS: " + dropsWorth);
            myGamesWithInfo.forEach(this::printMyDrops);
         } else {
            logger.info("--NO REMAINING DROPS");
         }
         logger.info("");
         logger.info("--TOTAL: " + totalWorth);
      } else {
         logger.info("NO CARDS OR DROPS");
      }

      long overStockedGames = myGamesWithInfo.stream().filter(this::isOverstocked).count();
      logger.info("");
      if (overStockedGames > 0) {
         logger.info("WARNING! The following of your games have overstocked cards in inventory!");
         myGamesWithInfo.forEach(this::printOverstocked);
         int ownedWorthNoOverstocked = myGamesWithInfo.stream().mapToInt(game -> isOverstocked(game) ? 0 : getMyAmount(game) * getCardPrice(game)).sum();
         logger.info("");
         logger.info("Worth of owned cards guaranteed not to be overstocked: " + ownedWorthNoOverstocked);

      } else {
         logger.info("None of your cards are overstocked.");
      }
   }

   private void printOverstocked(JsonObject game) {
      if (isOverstocked(game)) {
         logger.info(getGameName(game));
      }
   }

   private boolean isOverstocked(JsonObject game) {
      return getMyAmount(game) > 0 && game.get(PROP_GAME_MAX_CARD_STOCK).getAsInt() >= 8;
   }

   private JsonObject mergeGames(JsonObject game, JsonObject myGame) {
      game.addProperty(PROP_GAME_MY_AMOUNT, getMyAmount(myGame));
      game.addProperty(PROP_GAME_MY_DROPS, getMyDrops(myGame));
      return game;
   }

   private boolean gamesEqual(JsonObject game1, JsonObject game2) {
      return getGameName(game1).equals(getGameName(game2));
   }

   private JsonObject lineToGame(String line) {
      int colonPos1 = line.indexOf(':');
      if (colonPos1 <= 0) {
         return null;
      }
      int amount = Integer.parseInt(line.substring(0, colonPos1));
      if (amount < 0) {
         return null;
      }
      int colonPos2 = line.indexOf(':', colonPos1 + 1);
      int drops = Integer.parseInt(line.substring(colonPos1 + 1, colonPos2));
      if (drops < 0) {
         return null;
      }
      String name = line.substring(colonPos2 + 1);
      if (name.isEmpty()) {
         return null;
      }
      JsonObject game = new JsonObject();
      game.addProperty(PROP_GAME_NAME, name);
      game.addProperty(PROP_GAME_MY_AMOUNT, amount);
      game.addProperty(PROP_GAME_MY_DROPS, drops);
      return game;
   }

   private void printMyOwned(JsonObject game) {
      int myAmount = getMyAmount(game);
      printMyGame(game, myAmount);
   }

   private void printMyDrops(JsonObject game) {
      int myDrops = getMyDrops(game);
      printMyGame(game, myDrops);
   }

   private void printMyGame(JsonObject game, int amount) {
      int price = getCardPrice(game);
      if (amount > 0) {
         logger.info("(" + pad(getSetPrice(game), 3) + ") " +
               pad(amount, 2) + " * " +
               pad(price, 3) + " = " +
               pad(amount * price, 4) + " - " + getGameName(game));
      }
   }

   String pad(int nr, int minSpaces) {
      String result = "";
      while (nr < Math.pow(10, minSpaces - 1)) {
         result += " ";
         minSpaces--;
      }
      result += nr;
      return result;
   }

   private int getMyAmount(JsonObject game) {
      return game.get(PROP_GAME_MY_AMOUNT).getAsInt();
   }

   private int getMyDrops(JsonObject game) {
      return game.get(PROP_GAME_MY_DROPS).getAsInt();
   }

   private int getCardPrice(JsonObject game) {
      return game.get(PROP_GAME_CARDPRICE).getAsInt();
   }

   private int getSetPrice(JsonObject game) {
      return game.get(PROP_GAME_SET_PRICE).getAsInt();
   }

   private String getGameName(JsonObject game) {
      return game.get(PROP_GAME_NAME).getAsString();
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
