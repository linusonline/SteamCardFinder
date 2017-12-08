package se.lolektivet.steamcardfinder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lolektivet.steamcardfinder.MyOptions.*;

public class Analysis {

   private static final String PROP_GAME_NAME = "name";
   private static final String PROP_GAME_MY_AMOUNT = "myAmount";
   private static final String PROP_GAME_MY_DROPS = "myDrops";

   private static final Logger logger = Logger.getLogger(Analysis.class.getName());

   private final List<JsonElement> allGames = new ArrayList<>();

   private final CommandLine _commandLine;

   Analysis(CommandLine commandLine) {
      _commandLine = commandLine;
   }

   void analyze() throws IOException {
      String fullInput = new Input(_commandLine).getInput();
      doParseByHand(fullInput);
      if (_commandLine.hasOption(OPTION_MY_CARDS)) {
         doAnalyzeMyCards(_commandLine.getOptionValue(OPTION_MY_CARDS_FILE, DEFAULT_MY_CARDS_FILE));
      }
   }

   private void doParseByHand(String fullContent) {
      allGames.clear();

      logger.info("Parsing input JSON...");

      JsonParser jsonParser = new JsonParser();
      JsonArray rawGames = jsonParser.parse(fullContent).getAsJsonObject().get("data").getAsJsonArray();

      logger.info(rawGames.size() + " games in inventory.");
      logger.info("Analyzing...");
      for (JsonElement gameElement : rawGames) {
         allGames.add(gameElement);
      }

      List<JsonElement> sortedRelevantGames = allGames.stream()
            .filter(this::moreThanOneSet)
            .sorted(this::compareGames)
            .collect(Collectors.toList());

      try {
         // TODO: Document this feature.
         FileInputStream fis = new FileInputStream(DEFAULT_EXCLUDED_CARDS_FILE);
         BufferedReader excludesFile = new BufferedReader(new InputStreamReader(fis));
         List<String> excludedGames = excludesFile.lines().collect(Collectors.toList());
         sortedRelevantGames = sortedRelevantGames.stream()
               .filter(game -> excludedGames.stream().noneMatch(gameName -> gameName.equals(getGameName(game))))
               .collect(Collectors.toList());

      } catch (FileNotFoundException e) {
         logger.info("Could not open excludes file " + DEFAULT_EXCLUDED_CARDS_FILE + ". [" + e.getLocalizedMessage() + "]");
      }


      stdoutln("");
      stdoutln("---------------------- SteamCardExchange Inventory Summary --------------------");
      stdoutln("");
      stdoutln("Total number of games: " + allGames.size());

      stdoutln("Number of games with more than one full set: " + sortedRelevantGames.size());
      Stream<JsonElement> finalList = sortedRelevantGames.stream();
      if (_commandLine.hasOption(OPTION_GAME_LIMIT)) {
         int max = Integer.parseInt(_commandLine.getOptionValue(OPTION_GAME_LIMIT));
         stdoutln("Limiting list to " + max + " cheapest games.");
         finalList = finalList.limit(max);
      }
      stdoutln("");
      stdoutln("");
      stdoutln("");
      stdoutln("------------------------- Cheapest available Card Sets ------------------------");
      stdoutln("");
      stdoutln("[Setsize / Sets / Cost - Name]");
      finalList.forEach(this::printResult);
   }

   private int compareGames(JsonElement game1, JsonElement game2) {
      int priceCompare = Integer.compare(getSetPrice(game1), getSetPrice(game2));
      int setsCompare = Integer.compare(getFullSetsAvailable(game2), getFullSetsAvailable(game1));
      int setSizeCompare = Integer.compare(getUniqueCardsInSet(game1), getUniqueCardsInSet(game2));
      return priceCompare == 0 ? (setsCompare == 0 ? setSizeCompare : setsCompare) : priceCompare;
   }

   private boolean moreThanOneSet(JsonElement game) {
      return getFullSetsAvailable(game) > 1;
   }

   private void printResult(JsonElement game) {
      stdoutln(getUniqueCardsInSet(game) + " / " + getFullSetsAvailable(game) + " / " + getSetPrice(game) + " - " + getGameName(game));
   }

   private void doAnalyzeMyCards(String fileName) throws FileNotFoundException {
      FileInputStream fis = new FileInputStream(fileName);
      BufferedReader myCardsFile = new BufferedReader(new InputStreamReader(fis));

      List<JsonElement> myGamesWithInfo = myCardsFile.lines()
            .map(this::lineToGame)
            .filter(Objects::nonNull)
            .flatMap(myGame -> allGames.stream()
                  .filter(game -> gamesEqual(myGame, game))
                  .map(game -> mergeGames(game, myGame)))
            .collect(Collectors.toList());

      int credits = Integer.parseInt(_commandLine.getOptionValue(OPTION_MY_CREDITS, "0"));
      int ownedWorth = myGamesWithInfo.stream().mapToInt(game -> (getMyAmount(game) * getCardPrice(game))).sum();
      int dropsWorth = myGamesWithInfo.stream().mapToInt(game -> (getMyDrops(game) * getCardPrice(game))).sum();
      int totalWorth = ownedWorth + dropsWorth + credits;

      // TODO: Warn about possibly overstocked cards. Will not be precise based on the info we have,
      // but good enough to warn when "maxCardStock" is 8.
      //stdoutln("Sets you own that have overstocked cards:");

      stdoutln("");
      stdoutln("");
      stdoutln("");
      stdoutln("----------------------------- My Game Card Summary ----------------------------");
      stdoutln("");
      stdoutln("[(Set) Cards * Worth = Total - Name]");
      stdoutln("");
      if (totalWorth > 0) {
         if (ownedWorth > 0) {
            stdoutln("-- OWNED CARDS: " + ownedWorth);
            myGamesWithInfo.forEach(this::printMyOwned);
         } else {
            stdoutln("-- NO OWNED CARDS");
         }
         stdoutln("");
         if (dropsWorth > 0) {
            stdoutln("-- REMAINING DROPS: " + dropsWorth);
            myGamesWithInfo.forEach(this::printMyDrops);
         } else {
            stdoutln("-- NO REMAINING DROPS");
         }
         stdoutln("");
         if (credits > 0) {
            stdoutln("-- CREDITS: " + credits);
            stdoutln("");
         }
         stdoutln("-- TOTAL: " + totalWorth);
      } else {
         stdoutln("NO CARDS OR DROPS");
      }

      long overStockedGames = myGamesWithInfo.stream().filter(this::isOverstocked).count();
      stdoutln("");
      if (overStockedGames > 0) {
         stdoutln("WARNING! The following of your games have overstocked cards in inventory!");
         myGamesWithInfo.forEach(this::printOverstocked);
         int ownedWorthNoOverstocked = myGamesWithInfo.stream().mapToInt(game -> isOverstocked(game) ? 0 : getMyAmount(game) * getCardPrice(game)).sum();
         int dropWorthNoOverstocked = myGamesWithInfo.stream().mapToInt(game -> isOverstocked(game) ? 0 : getMyDrops(game) * getCardPrice(game)).sum();
         stdoutln("");
         stdoutln("Excluding possibly overstocked cards:");
         stdoutln("Owned card worth: " + ownedWorthNoOverstocked);
         stdoutln("Drops worth: " + dropWorthNoOverstocked);
         stdoutln("Total worth: " + (ownedWorthNoOverstocked + credits));

      } else {
         stdoutln("None of your cards are overstocked.");
      }
   }

   private void printOverstocked(JsonElement game) {
      if (isOverstocked(game)) {
         stdoutln(getGameName(game));
      }
   }

   private boolean isOverstocked(JsonElement game) {
      return (getMyAmount(game) > 0 || getMyDrops(game) > 0) && getHighestCardStock(game) >= 8;
   }

   private JsonElement mergeGames(JsonElement game, JsonObject myGame) {
      game.getAsJsonArray().add(myGame.get(PROP_GAME_MY_AMOUNT).getAsInt());
      game.getAsJsonArray().add(myGame.get(PROP_GAME_MY_DROPS).getAsInt());
      return game;
   }

   private boolean gamesEqual(JsonObject myGame, JsonElement otherGame) {
      return myGame.get(PROP_GAME_NAME).getAsString().equals(getGameName(otherGame));
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

   private void printMyOwned(JsonElement game) {
      int myAmount = getMyAmount(game);
      printMyGame(game, myAmount);
   }

   private void printMyDrops(JsonElement game) {
      int myDrops = getMyDrops(game);
      printMyGame(game, myDrops);
   }

   private void printMyGame(JsonElement game, int amount) {
      int price = getCardPrice(game);
      if (amount > 0) {
         stdoutln("(" + pad(getSetPrice(game), 3) + ") " +
               pad(amount, 2) + " * " +
               pad(price, 3) + " = " +
               pad(amount * price, 4) + " - " + getGameName(game));
      }
   }

   static String pad(int nr, int minSpaces) {
      String result = "";
      while (nr < Math.pow(10, minSpaces - 1)) {
         result += " ";
         minSpaces--;
      }
      result += nr;
      return result;
   }

   private int getMyAmount(JsonElement game) {
      return game.getAsJsonArray().get(4).getAsInt();
   }

   private int getMyDrops(JsonElement game) {
      return game.getAsJsonArray().get(5).getAsInt();
   }



   private int getSetPrice(JsonElement game) {
      return getCardPrice(game) * getUniqueCardsInSet(game);
   }

   private int getCardPrice(JsonElement game) {
      return game.getAsJsonArray().get(1).getAsInt();
   }

   private int getUniqueCardsInSet(JsonElement game) {
      return game.getAsJsonArray().get(3).getAsJsonArray().get(0).getAsInt();
   }

   private int getUniqueCardsAvailable(JsonElement game) {
      return game.getAsJsonArray().get(3).getAsJsonArray().get(1).getAsInt();
   }

   private int getFullSetsAvailable(JsonElement game) {
      return getUniqueCardsAvailable(game) < getUniqueCardsInSet(game) ? 0 :
            game.getAsJsonArray().get(3).getAsJsonArray().get(2).getAsInt();
   }

   private String getGameName(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(1).getAsString();
   }

   private int getHighestCardStock(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(2).getAsInt();
   }

   private int getGameId(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsInt();
   }

   private boolean isMarketable(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(4).getAsInt() != 0;
   }

   static void stdoutln(String message) {
      System.out.println(message);
   }
}
