/*
 * 
 */
package jj.uritemplate.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * A TestVariableExpansion.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestVariableExpansion extends AbstractUriTemplateConformanceTest
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
      return loadTestData("variable-expansion.json");
   }
   
   /**
    * 
    * Create a new TestVariableExpansion.
    * 
    * @param vars
    * @param expression
    * @param expected
    * @param testsuite
    */
   public TestVariableExpansion(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

}
