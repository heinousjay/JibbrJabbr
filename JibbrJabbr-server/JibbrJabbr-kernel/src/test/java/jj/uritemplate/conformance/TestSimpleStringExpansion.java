/*
 * 
 */
package jj.uritemplate.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * A TestSimpleStringExpansion.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestSimpleStringExpansion extends AbstractUriTemplateConformanceTest
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
      return loadTestData("simple-string-expansion.json");
   }
   /**
    * 
    * Create a new TestSimpleStringExpansion.
    * 
    * @param vars
    * @param expression
    * @param expected
    * @param testsuite
    */
   public TestSimpleStringExpansion(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

}
