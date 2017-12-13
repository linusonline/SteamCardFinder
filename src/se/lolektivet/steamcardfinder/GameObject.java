package se.lolektivet.steamcardfinder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GameObject {
   private static final String PROP_MYGAME_NAME = "name";
   private static final String PROP_MYGAME_AMOUNT = "myAmount";
   private static final String PROP_MYGAME_DROPS = "myDrops";

   static JsonObject createMyGame(String name, int amount, int drops) {
      JsonObject game = new JsonObject();
      game.addProperty(GameObject.PROP_MYGAME_NAME, name);
      game.addProperty(GameObject.PROP_MYGAME_AMOUNT, amount);
      game.addProperty(GameObject.PROP_MYGAME_DROPS, drops);
      return game;
   }

   static boolean gamesEqual(JsonObject myGame, JsonElement otherGame) {
      return myGame.get(PROP_MYGAME_NAME).getAsString().equals(getGameName(otherGame));
   }

   static JsonElement mergeMyGameIntoGame(JsonElement game, JsonObject myGame) {
      game.getAsJsonArray().add(myGame.get(PROP_MYGAME_AMOUNT).getAsInt());
      game.getAsJsonArray().add(myGame.get(PROP_MYGAME_DROPS).getAsInt());
      return game;
   }

   static boolean isOverstocked(JsonElement game) {
      return (getMyAmount(game) > 0 || getMyDrops(game) > 0) && getHighestCardStock(game) >= 8;
   }

   static int getMyAmount(JsonElement game) {
      return game.getAsJsonArray().get(4).getAsInt();
   }

   static int getMyDrops(JsonElement game) {
      return game.getAsJsonArray().get(5).getAsInt();
   }


   // Game element "primitives"

   static String getGameName(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(1).getAsString();
   }

   static int getSetPrice(JsonElement game) {
      return getCardPrice(game) * getUniqueCardsInSet(game);
   }

   static int getMaxSetPrice(JsonElement game) {
      return getLastCardPrice(game) * getUniqueCardsInSet(game);
   }

   static int getCardPrice(JsonElement game) {
      return game.getAsJsonArray().get(1).getAsInt();
   }

   static int getLastCardPrice(JsonElement game) {
      return (int)Math.ceil(getCardPrice(game) * 1.5);
   }

   static int getTotalCardsInStock(JsonElement game) {
      return game.getAsJsonArray().get(2).getAsInt();
   }

   static int getUniqueCardsInSet(JsonElement game) {
      return game.getAsJsonArray().get(3).getAsJsonArray().get(0).getAsInt();
   }

   static int getUniqueCardsAvailable(JsonElement game) {
      return game.getAsJsonArray().get(3).getAsJsonArray().get(1).getAsInt();
   }

   static int getFullSetsAvailable(JsonElement game) {
      return getUniqueCardsAvailable(game) < getUniqueCardsInSet(game) ? 0 :
            game.getAsJsonArray().get(3).getAsJsonArray().get(2).getAsInt();
   }

   static int getHighestCardStock(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(2).getAsInt();
   }

   private static int getGameId(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsInt();
   }

   private static boolean isMarketable(JsonElement game) {
      return game.getAsJsonArray().get(0).getAsJsonArray().get(4).getAsInt() != 0;
   }

   // JSON game object anatomy:
   // [
   //    [449940,"! That Bastard Is Trying To Steal Our Gold !",4,1,1],
   //    8,
   //    14,
   //    [5,5,2]
   // ],
   // [
   //    [<int:ID>,<string:name>,<int:highestStock>,<int:???>,<int:marketable>],
   //    <int:cardprice>,
   //    <int:totalNrOfCards>,
   //    [<int:setsize>,<int:uniqueAvailable>,<int:setsAvailable>]
   // ],

}
