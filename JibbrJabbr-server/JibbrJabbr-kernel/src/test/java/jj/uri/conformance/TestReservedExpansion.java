/*
 * 
 */
package jj.uri.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * A TestReservedExpansion.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestReservedExpansion extends AbstractUriTemplateConformanceTest
{

   /**
    * FIXME Comment this
    * 
    * @return
    * @throws Exception
    */
   @Parameters
   public static Collection<Object[]> testData() throws Exception
   {
      return loadTestData("reserved-expansion.json");
   }

   /**
    * 
    * Create a new TestReservedExpansion.
    * 
    * @param vars
    * @param expression
    * @param expected
    * @param testsuite
    */
   public TestReservedExpansion(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      // FIXME TestReservedExpansion constructor
      super(vars, template, expected, testsuite);
   }

}
