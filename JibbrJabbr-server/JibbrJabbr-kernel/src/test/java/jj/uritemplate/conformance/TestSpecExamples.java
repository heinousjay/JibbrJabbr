/*
 * 
 */
package jj.uritemplate.conformance;

import java.util.Collection;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class TestSpecExamples extends AbstractUriTemplateConformanceTest
{
   
   @Parameters
   public static Collection<Object[]> testData() throws Exception
   {
      return loadTestData("spec-examples.json");
   }

   public TestSpecExamples(Map<String, Object> vars, String template, Object expected, String testsuite)
   {
      super(vars, template, expected, testsuite);
   }

}
