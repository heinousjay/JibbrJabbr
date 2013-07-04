/*
 * 
 */
package jj.uritemplate;

import static org.junit.Assert.*;
import jj.uritemplate.UriTemplate;

import org.junit.Test;

/**
 * A TestExplodeWithNonStringValues.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestExplodeWithNonStringValues
{
   private static final String expression = "/{foo:1}/{foo}";

   @Test
   public void testExpandInteger() throws Exception
   {
      String result = UriTemplate.fromTemplate(expression).set("foo", new Integer(300)).expand();
      assertEquals("/3/300", result);
   }
}
