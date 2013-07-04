/*
 * 
 */
package jj.uritemplate;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Map;
import java.util.Map.Entry;

import jj.uritemplate.UriTemplate;

/**
 * Simple tests to validate the UriTemplate API.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestCreateUriTemplate
{

   @Test
   public void testFromTemplate() throws Exception
   {
      UriTemplate base = new UriTemplate("http://myhost{/version,myId}")
                                    .set("myId","damnhandy")
                                    .set("version","v1"); // URI versioning is silly, but this is just for examples
      
      UriTemplate child = new UriTemplate(base)
                                     .append("/things/{thingId}")
                                     .set("thingId","123öä|\\");
      
      
      assertEquals(3, child.values().size());
      Map<String, Object> childValues = child.values();
      for(Entry<String, Object> e : base.values().entrySet())
      {
         assertTrue(childValues.containsKey(e.getKey()));
         assertEquals(e.getValue(), childValues.get(e.getKey()));
      }
      assertEquals("http://myhost/v1/damnhandy/things/123%C3%B6%C3%A4%7C%5C", child.expand());
   }
   
   
   @Test
   public void testMultpleExpressions() throws Exception
   {
      UriTemplate template = new UriTemplate("http://myhost")
                                        .append("{/version}")
                                        .append("{/myId}")
                                        .append(null)
                                        .append(" ")
                                        .append("/things/{thingId}")
                                        .set("myId","damnhandy")
                                        .set("version","v1")
                                        .set("thingId","12345");
      
      assertEquals(3, template.values().size());
      assertEquals("http://myhost{/version}{/myId}/things/{thingId}", template.getTemplate());
      assertEquals("http://myhost/v1/damnhandy/things/12345", template.expand());
   }
}
