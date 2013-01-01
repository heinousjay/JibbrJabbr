/*
 * 
 */
package jj.uri;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;


/**
 * Simple test to make sure that the properties of an object are correctly 
 * translated to {@link Map} and that the annotations are properly controlling
 * the label names or ensuring that the field is not included in the value list.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestDefaultVarExploder
{

   /**
    * 
    * 
    * @throws Exception
    */
   @Test
   public void testBasic() throws Exception
   {
      Address address = new Address("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      address.setIgnored("This should be ignored");
      VarExploder exploder = new VarExploder(address);
      Map<String, Object> values = exploder.getNameValuePairs();
      
      assertTrue(values.containsKey("street"));
      assertTrue(values.containsKey("city"));
      assertTrue(values.containsKey("zipcode"));
      assertFalse(values.containsKey("postalCode"));
      assertTrue(values.containsKey("state"));
      assertTrue(values.containsKey("country"));
      assertFalse(values.containsKey("ignored"));
      assertEquals("4 Yawkey Way", values.get("street"));
      assertEquals("Boston", values.get("city"));
      assertEquals("MA", values.get("state"));
      assertEquals("02215-3496", values.get("zipcode"));
      assertEquals("USA", values.get("country"));
      
      List<Object> list = new LinkedList<Object>(exploder.getValues());
      assertEquals("4 Yawkey Way", list.get(3));
      assertEquals("Boston", list.get(0));
      assertEquals("MA", list.get(2));
      assertEquals("02215-3496", list.get(4));
      assertEquals("USA", list.get(1));
   }
   
   
   @Test
   public void testWithSubclass() throws Exception
   {
      ExtendedAddress address = new ExtendedAddress("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      address.setIgnored("This should be ignored");
      address.setLabel("A label");
      VarExploder exploder = new VarExploder(address);
      Map<String, Object> values = exploder.getNameValuePairs();
      
      assertTrue(values.containsKey("street"));
      assertTrue(values.containsKey("city"));
      assertTrue(values.containsKey("zipcode"));
      assertFalse(values.containsKey("postalCode"));
      assertTrue(values.containsKey("state"));
      assertTrue(values.containsKey("country"));
      assertTrue(values.containsKey("label"));
      assertFalse(values.containsKey("ignored"));

      assertEquals("4 Yawkey Way", values.get("street"));
      assertEquals("Boston", values.get("city"));
      assertEquals("MA", values.get("state"));
      assertEquals("02215-3496", values.get("zipcode"));
      assertEquals("USA", values.get("country"));
      assertEquals("A label", values.get("label"));

   }
}
