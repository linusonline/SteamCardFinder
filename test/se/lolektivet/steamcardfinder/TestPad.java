package se.lolektivet.steamcardfinder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Linus on 2017-03-25.
 */
public class TestPad {

   @Test
   public void testPad1() {
      String res = Analysis.pad(1, 1);
      assertEquals("1", res);
   }

   @Test
   public void testPad2() {
      String res = Analysis.pad(9, 1);
      assertEquals("9", res);
   }

   @Test
   public void testPad3() {
      String res = Analysis.pad(1, 2);
      assertEquals(" 1", res);
   }

   @Test
   public void testPadZero1() {
      String res = Analysis.pad(0, 1);
      assertEquals("0", res);
   }

   @Test
   public void testPadZero2() {
      String res = Analysis.pad(0, 2);
      assertEquals(" 0", res);
   }
}
