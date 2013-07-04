/*
 * 
 */
package jj.uritemplate.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * A TestPathStyleParameterExpansion.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestPathStyleParameterExpansion extends AbstractUriTemplateConformanceTest
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
      return loadTestData("path-style-parameter-expansion.json");
   }

   /**
    * 
    * Create a new TestPathStyleParameterExpansion.
    * 
    * @param vars
    * @param expression
    * @param expected
    * @param testsuite
    */
   public TestPathStyleParameterExpansion(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

}
