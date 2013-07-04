/*
 * 
 */
package jj.uritemplate;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jj.uritemplate.UriTemplate;
import jj.uritemplate.VarExploder;

import org.junit.Test;


/**
 * 
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestExplodeWithPOJO
{

   private static final String EXPLODE_TEMPLATE = "/mapper{?address*}";

   private static final String NON_EXPLODE_TEMPLATE = "/mapper{?address}";

   @Test
   public void testExplodeAddress() throws Exception
   {
      Address address = new Address();
      address.setState("CA");
      address.setCity("Newport Beach");
      String result = UriTemplate.fromTemplate(EXPLODE_TEMPLATE).set("address", address).expand();

      assertEquals("/mapper?city=Newport%20Beach&state=CA", result);
   }

   @Test
   public void testExplodeAddressWithNoExplodeOperator() throws Exception
   {
      Address address = new Address("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      String result = UriTemplate.fromTemplate(NON_EXPLODE_TEMPLATE).set("address", address).expand();
      assertEquals("/mapper?address=Boston,USA,MA,4%20Yawkey%20Way,02215-3496", result);
   }

   /**
    * 
    * 
    * @throws Exception
    */
   @Test
   public void testSimpleAddress() throws Exception
   {
      Address address = new Address("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      String result = UriTemplate.fromTemplate(EXPLODE_TEMPLATE).set("address", address).expand();
      assertEquals("/mapper?city=Boston&country=USA&state=MA&street=4%20Yawkey%20Way&zipcode=02215-3496", result);
   }

   @Test
   public void testExplodeWithSubclass() throws Exception
   {
      ExtendedAddress address = new ExtendedAddress("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      address.setIgnored("This should be ignored");
      address.setLabel("A label");
      String result = UriTemplate.fromTemplate(EXPLODE_TEMPLATE).set("address", address).expand();

      assertEquals(
            "/mapper?city=Boston&country=USA&label=A%20label&state=MA&street=4%20Yawkey%20Way&zipcode=02215-3496",
            result);
   }

   @Test
   public void testWrappedExploder()
   {
      Address address = new Address("4 Yawkey Way", "Boston", "MA", "02215-3496", "USA");
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("address", new VarExploder(address));
      String result = UriTemplate.expand(EXPLODE_TEMPLATE, values);
      assertEquals("/mapper?city=Boston&country=USA&state=MA&street=4%20Yawkey%20Way&zipcode=02215-3496", result);

   }
}
