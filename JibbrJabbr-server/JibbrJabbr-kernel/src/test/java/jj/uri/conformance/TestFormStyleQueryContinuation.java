/*
 * 
 */
package jj.uri.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * A TestFormStyleQueryContinuation.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestFormStyleQueryContinuation extends AbstractUriTemplateConformanceTest
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
      return loadTestData("form-style-query-continuation.json");
   }

   /**
    * 
    * Create a new TestFormStyleQueryContinuation.
    * 
    * @param vars
    * @param expression
    * @param expected
    * @param testsuite
    */
   public TestFormStyleQueryContinuation(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

}
