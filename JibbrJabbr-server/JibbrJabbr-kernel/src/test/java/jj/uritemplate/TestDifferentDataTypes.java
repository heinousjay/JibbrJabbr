/*
 * 
 */
package jj.uritemplate;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jj.uritemplate.UriTemplate;
import jj.uritemplate.VariableExpansionException;

import org.junit.Test;

/**
 *
 * 
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */

public class TestDifferentDataTypes
{
   private static final int[] INT_COUNT = {1, 2, 3};

   private static final Integer[] INTEGER_COUNT = {new Integer(1), new Integer(2), new Integer(3)};

   private static final long[] LONG_COUNT = {1l, 2l, 3l};

   private static final float[] FLOAT_COUNT = {1.01f, 2.02f, 3.03f};

   private static final double[] DOUBLE_COUNT = {1.02d, 2.04d, 3.05d};

   private static final String TEMPLATE_1 = "{count}";

   private static final String TEMPLATE_2 = "{count*}";

   private static final char[] CHAR_ARRAY = {'o', 'n', 'e'};

   private static final char[][] MULTI_CHAR_ARRAY = {CHAR_ARRAY, {'t', 'w', 'o'}};
   
   private static final boolean FLAG = false;

   @Test
   public void testBooleanPrimitive() throws Exception {
      assertEquals("/set?flag=false", UriTemplate.fromTemplate("/set{?flag}").set("flag", FLAG).expand());
   }
   
   @Test
   public void testBooleanObject() throws Exception {
      assertEquals("/set?flag=false", UriTemplate.fromTemplate("/set{?flag}").set("flag", Boolean.FALSE).expand());
   }
   
   @Test
   public void testInts() throws Exception {
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_1).set("count", INT_COUNT).expand());
   }
   
   @Test
   public void testIntegers() throws Exception {
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_1).set("count", INTEGER_COUNT).expand());
   }
  
   @Test
   public void testLongs() throws Exception {
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_1).set("count", LONG_COUNT).expand());
   }
   
   @Test
   public void testFloats() throws Exception {
      assertEquals("1.01,2.02,3.03", UriTemplate.fromTemplate(TEMPLATE_1).set("count", FLOAT_COUNT).expand());
   }
   
   @Test
   public void testDoubles() throws Exception {
      assertEquals("1.02,2.04,3.05", UriTemplate.fromTemplate(TEMPLATE_1).set("count", DOUBLE_COUNT).expand());
   }
   
   @Test
   public void testExplodeDoubles() throws Exception {
      assertEquals("1.02,2.04,3.05", UriTemplate.fromTemplate(TEMPLATE_2).set("count", DOUBLE_COUNT).expand());
   }
   
   @Test
   public void testCharArray() throws Exception {
      assertEquals("one", UriTemplate.fromTemplate(TEMPLATE_1).set("count", CHAR_ARRAY).expand());
   }
   
   @Test
   public void testMultiCharArray() throws Exception {
      assertEquals("one,two", UriTemplate.fromTemplate(TEMPLATE_1).set("count", MULTI_CHAR_ARRAY).expand());
   }
   
   @Test
   public void test() throws Exception {
      
   }
   
   @Test
   public void testTypes() throws Exception
   {
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_2).set("count", INT_COUNT).expand());
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_2).set("count", INTEGER_COUNT).expand());
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_2).set("count", LONG_COUNT).expand());
      assertEquals("1.01,2.02,3.03", UriTemplate.fromTemplate(TEMPLATE_2).set("count", FLOAT_COUNT).expand());
      assertEquals("1,2,3", UriTemplate.fromTemplate(TEMPLATE_2).set("count", INT_COUNT).expand());      
   }

   @Test(expected = VariableExpansionException.class)
   public void testMultiDimensionalArray() throws Exception
   {
      String[][] values = {{"one", "two"}, {"three", "four"}};
      UriTemplate.fromTemplate(TEMPLATE_1).set("count", values).expand();
   }
   
   @Test(expected = VariableExpansionException.class)
   public void testNestedCollections() throws Exception
   {
      List<List<String>> values = new ArrayList<List<String>>();
      List<String> one = new ArrayList<String>();
      one.add("One");
      one.add("Two");
      values.add(one);
      List<String> two = new ArrayList<String>();
      two.add("Three");
      two.add("Four");
      values.add(two);
      String uri = UriTemplate.fromTemplate(TEMPLATE_2).set("count", values).expand();
      System.out.println(uri);
   }
   
   /**
    * 
    * 
    * @throws Exception
    */
   @Test(expected = VariableExpansionException.class)
   public void testMapWithNestedCollections() throws Exception
   {
      Map<String, List<String>> values = new HashMap<String,List<String>>();
      List<String> one = new ArrayList<String>();
      one.add("One");
      one.add("Two");
      values.put("one",one);
      List<String> two = new ArrayList<String>();
      two.add("Three");
      two.add("Four");
      values.put("two",two);
      String uri = UriTemplate.fromTemplate(TEMPLATE_2).set("count", values).expand();
      System.out.println(uri);
   }
}
