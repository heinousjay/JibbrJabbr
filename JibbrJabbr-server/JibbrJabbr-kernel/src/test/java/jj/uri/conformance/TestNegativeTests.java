/*
 * 
 */
package jj.uri.conformance;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;

import jj.uri.ExpressionParseException;
import jj.uri.UriTemplate;
import jj.uri.VariableExpansionException;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;


/**
 * 
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestNegativeTests extends AbstractUriTemplateConformanceTest
{
   @Parameters
   public static Collection<Object[]> testData() throws Exception
   {
      return loadTestData("negative-tests.json");
   }

   public TestNegativeTests(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

   /**
    * 
    * @throws Exception
    */
   @Test
   public void test() throws Exception
   {
      UriTemplate t = UriTemplate.fromTemplate(template);
      boolean pass = true;
      try
      {
         String actual = t.expand(variables);
         System.out.println(actual);
      }
      catch (ExpressionParseException e)
      {
         pass = false;
      }

      catch (VariableExpansionException e)
      {
         pass = false;
      }
      assertFalse("Expected "+template+" to fail",pass);
   }
}
